from __future__ import annotations

import argparse
import json
import socket
import subprocess
import sys
import time
import urllib.error
import urllib.request
import webbrowser
from pathlib import Path


ROOT = Path(__file__).resolve().parent
SCRIPTS = ROOT / "scripts"
BACKEND_HEALTH_URL = "http://localhost:8080/actuator/health"
FRONTEND_URL = "http://localhost:5173/"
CREATE_NEW_CONSOLE = 0x00000010


def main() -> int:
    configure_console()
    parser = argparse.ArgumentParser(
        description="启动 kaoshi 本地开发环境。默认只启动；加 --reset 会清空 Docker 数据库后启动。"
    )
    parser.add_argument("--reset", action="store_true", help="先清空 Docker MySQL/Redis 数据，再启动")
    args = parser.parse_args()

    print_step("kaoshi 本地启动器")
    print("这个脚本会帮你启动 Docker 数据库、Java 后端、Vue 前端，然后打开浏览器。")
    print("前端页面：http://localhost:5173/")
    print("后端服务：http://localhost:8080/")
    print("登录账号：admin")
    print("登录密码：password")

    if args.reset:
        print_step("清空启动模式")
        print("会先停止本项目所有本地服务，再清空 Docker 里的 kaoshi 数据。")
        run_powershell_script(SCRIPTS / "stop-dev.ps1")
        run_powershell_script(SCRIPTS / "reset-docker-data.ps1")
    else:
        print_step("普通启动模式")
        print("不会清空数据库。会先停止本项目旧服务，再重新打开新的运行窗口。")
        run_powershell_script(SCRIPTS / "stop-dev.ps1")
        run_powershell_script(SCRIPTS / "dev.ps1")

    print_step("启动 Java 后端")
    print("会打开一个新的 PowerShell 窗口。那个窗口不要关，它就是后端服务。")
    start_powershell_window("kaoshi backend", SCRIPTS / "run-backend.ps1")

    print_step("启动 Vue 前端")
    print("会打开一个新的 PowerShell 窗口。那个窗口不要关，它就是前端页面服务。")
    start_powershell_window("kaoshi frontend", SCRIPTS / "run-frontend.ps1")

    wait_for_backend()
    wait_for_http(FRONTEND_URL, "frontend")
    print_step("打开浏览器")
    webbrowser.open(FRONTEND_URL, new=1)
    print_success("启动完成。浏览器打开后，用 admin / password 登录。")
    return 0


def run_powershell_script(script: Path) -> None:
    print(f"正在执行：{script.relative_to(ROOT)}")
    command = ["powershell", "-ExecutionPolicy", "Bypass", "-File", str(script)]
    subprocess.run(command, cwd=ROOT, check=True)


def start_powershell_window(title: str, script: Path) -> None:
    command = (
        f"$Host.UI.RawUI.WindowTitle = '{title}'; "
        f"& '{script}'; "
        "Write-Host ''; "
        "Read-Host 'Process exited. Press Enter to close this window'"
    )
    subprocess.Popen(
        ["powershell", "-ExecutionPolicy", "Bypass", "-Command", command],
        cwd=ROOT,
        creationflags=CREATE_NEW_CONSOLE,
    )
    print_success(f"已打开窗口：{title}")


def stop_project_port(port: int, service_name: str) -> None:
    if not is_tcp_port_open("127.0.0.1", port):
        print(f"{service_name} 端口 {port} 没有旧服务。")
        return

    print(f"{service_name} 端口 {port} 已被占用，正在关闭旧服务...")
    command = (
        f"Get-NetTCPConnection -LocalPort {port} -State Listen -ErrorAction SilentlyContinue "
        "| Select-Object -ExpandProperty OwningProcess -Unique "
        "| ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }"
    )
    subprocess.run(["powershell", "-ExecutionPolicy", "Bypass", "-Command", command], cwd=ROOT, check=False)
    deadline = time.time() + 20
    while time.time() < deadline:
        if not is_tcp_port_open("127.0.0.1", port):
            return
        time.sleep(0.5)
    raise RuntimeError(f"端口 {port} 仍然被占用。请手动关闭占用该端口的窗口或程序后再试。")


def wait_for_backend(timeout_seconds: int = 180) -> None:
    print("等待 Java 后端健康检查通过...")
    deadline = time.time() + timeout_seconds
    while time.time() < deadline:
        if is_backend_ready():
            print_success("Java 后端健康检查通过。")
            return
        time.sleep(2)
    raise TimeoutError("等待 Java 后端超时。请查看名为 kaoshi backend 的 PowerShell 窗口里的错误。")


def wait_for_http(url: str, name: str, timeout_seconds: int = 180) -> None:
    display_name = "Vue 前端" if name == "frontend" else name
    print(f"等待 {display_name} 可访问...")
    deadline = time.time() + timeout_seconds
    while time.time() < deadline:
        if is_http_ready(url):
            print_success(f"{display_name} 已可访问。")
            return
        time.sleep(2)
    raise TimeoutError(f"等待 {display_name} 超时。请查看名为 kaoshi frontend 的 PowerShell 窗口里的错误。")


def is_backend_ready() -> bool:
    try:
        with urllib.request.urlopen(BACKEND_HEALTH_URL, timeout=3) as response:
            payload = json.loads(response.read().decode("utf-8"))
            return response.status == 200 and payload.get("status") == "UP"
    except (OSError, ValueError, urllib.error.URLError):
        return False


def is_http_ready(url: str) -> bool:
    try:
        with urllib.request.urlopen(url, timeout=3) as response:
            return 200 <= response.status < 500
    except (OSError, urllib.error.URLError):
        return False


def is_tcp_port_open(host: str, port: int) -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.settimeout(1)
        return sock.connect_ex((host, port)) == 0


def print_step(message: str) -> None:
    print("")
    print(f"== {message} ==")


def print_success(message: str) -> None:
    print(f"[完成] {message}")


def configure_console() -> None:
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8", errors="replace")
        except AttributeError:
            pass


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except KeyboardInterrupt:
        print("启动已取消。")
        raise SystemExit(130)
    except Exception as exc:
        print("", file=sys.stderr)
        print("[启动失败]", file=sys.stderr)
        print(str(exc), file=sys.stderr)
        print("", file=sys.stderr)
        print("你可以按下面顺序排查：", file=sys.stderr)
        print("1. 确认 Docker Desktop 已经打开。", file=sys.stderr)
        print("2. 确认没有手动关闭新打开的 backend/frontend 窗口。", file=sys.stderr)
        print("3. 如果想恢复初始数据，运行：python .\\start_dev.py --reset", file=sys.stderr)
        print("4. 如果仍失败，把当前终端和新窗口里的报错发给 Codex。", file=sys.stderr)
        raise SystemExit(1)

