from __future__ import annotations

import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parent


def main() -> int:
    configure_console()
    print("")
    print("== kaoshi 测试停止器 ==")
    subprocess.run(["powershell", "-ExecutionPolicy", "Bypass", "-File", str(ROOT / "scripts" / "stop-dev.ps1")], cwd=ROOT, check=True)
    print("[完成] 测试相关服务已停止。")
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
        print("停止已取消。")
        raise SystemExit(130)
    except Exception as exc:
        print("", file=sys.stderr)
        print("[停止失败]", file=sys.stderr)
        print(str(exc), file=sys.stderr)
        raise SystemExit(1)
