from __future__ import annotations

import subprocess
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path


ROOT = Path(__file__).resolve().parent
FRONTEND = ROOT / "frontend"
BACKEND_HEALTH_URL = "http://127.0.0.1:8080/actuator/health"
FRONTEND_URL = "http://127.0.0.1:5174/"
E2E_LOG_DIR = ROOT / "logs" / "e2e"


def main() -> int:
    configure_console()
    started_at = time.perf_counter()
    print("")
    print("== kaoshi 真实浏览器测试 ==")
    print("会先停止旧测试服务，重置 Docker 数据库，启动真实后端和前端，然后运行有头 Chromium 自动测试。")
    run_step("停止旧测试服务", lambda: subprocess.run(["powershell", "-ExecutionPolicy", "Bypass", "-File", str(ROOT / "scripts" / "stop-dev.ps1")], cwd=ROOT, check=True))
    E2E_LOG_DIR.mkdir(parents=True, exist_ok=True)
    stack_out = (E2E_LOG_DIR / "browser-stack.out.log").open("w", encoding="utf-8", errors="replace")
    stack_err = (E2E_LOG_DIR / "browser-stack.err.log").open("w", encoding="utf-8", errors="replace")
    stack = subprocess.Popen(
        ["powershell", "-ExecutionPolicy", "Bypass", "-File", str(ROOT / "scripts" / "run-e2e-stack.ps1")],
        cwd=ROOT,
        stdout=stack_out,
        stderr=stack_err,
    )
    try:
        run_step("等待后端健康检查", lambda: wait_for_http(BACKEND_HEALTH_URL, stack, "后端健康检查"))
        run_step("等待前端页面", lambda: wait_for_http(FRONTEND_URL, stack, "前端页面"))
        run_step("运行 Playwright 浏览器用例", lambda: subprocess.run(["npm.cmd", "run", "test:e2e"], cwd=FRONTEND, check=True))
    finally:
        if stack.poll() is None:
            stack.terminate()
            try:
                stack.wait(timeout=10)
            except subprocess.TimeoutExpired:
                stack.kill()
        stack_out.close()
        stack_err.close()
        run_step("清理测试服务", lambda: subprocess.run(["powershell", "-ExecutionPolicy", "Bypass", "-File", str(ROOT / "scripts" / "stop-dev.ps1")], cwd=ROOT, check=False))
    print(f"[完成] 真实浏览器测试通过。总耗时 {format_elapsed(time.perf_counter() - started_at)}。")
    return 0


def wait_for_http(url: str, stack: subprocess.Popen[bytes], name: str, timeout_seconds: int = 300) -> None:
    print(f"等待真实测试环境可访问：{name}...")
    deadline = time.time() + timeout_seconds
    while time.time() < deadline:
        if stack.poll() is not None:
            raise RuntimeError(f"真实测试环境启动失败，退出码：{stack.returncode}。请查看 logs\\e2e\\browser-stack.out.log 和 browser-stack.err.log。")
        try:
            with urllib.request.urlopen(url, timeout=3) as response:
                if 200 <= response.status < 500:
                    print(f"[完成] {name} 已可访问。")
                    return
        except (OSError, urllib.error.URLError):
            time.sleep(2)
    raise TimeoutError(f"等待真实测试环境超时：{name}。请查看 logs\\e2e 里的 backend/frontend 日志。")


def configure_console() -> None:
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8", errors="replace")
        except AttributeError:
            pass


def run_step(name: str, action) -> None:
    started_at = time.perf_counter()
    print(f"== {name} ==")
    try:
        action()
    finally:
        print(f"[耗时] {name}: {format_elapsed(time.perf_counter() - started_at)}")


def format_elapsed(seconds: float) -> str:
    minutes, remain = divmod(int(seconds), 60)
    if minutes:
        return f"{minutes}分{remain}秒"
    return f"{remain}秒"


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except KeyboardInterrupt:
        print("浏览器测试已取消。")
        raise SystemExit(130)
    except Exception as exc:
        print("", file=sys.stderr)
        print("[浏览器测试失败]", file=sys.stderr)
        print(str(exc), file=sys.stderr)
        print("如果浏览器或服务窗口仍在运行，可以执行：python .\\stop_test.py", file=sys.stderr)
        raise SystemExit(1)
