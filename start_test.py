from __future__ import annotations

import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parent


def main() -> int:
    configure_console()
    print("")
    print("== kaoshi 常规测试 ==")
    print("会先停止本项目旧服务，然后运行后端测试、前端类型检查、前端单元测试、前端构建和文档扫描。")
    subprocess.run(
        ["powershell", "-ExecutionPolicy", "Bypass", "-File", str(ROOT / "scripts" / "stop-dev.ps1")],
        cwd=ROOT,
        check=True,
    )
    subprocess.run(
        ["powershell", "-ExecutionPolicy", "Bypass", "-File", str(ROOT / "scripts" / "verify.ps1")],
        cwd=ROOT,
        check=True,
    )
    print("[完成] 常规测试通过。")
    return 0


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
        print("测试已取消。")
        raise SystemExit(130)
    except Exception as exc:
        print("", file=sys.stderr)
        print("[测试失败]", file=sys.stderr)
        print(str(exc), file=sys.stderr)
        raise SystemExit(1)
