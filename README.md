# flink-rce-by-design

## Overview
This repository contains a Proof of Concept (PoC) for exploiting an RCE-by-design in Apache Flink Dashboard. Since Flink allows users to upload and execute custom JAR files by design, an exposed dashboard without proper Access Control (RBAC) or authentication leads to immediate **Remote Code Execution (RCE)**.

In hardened environments where outbound network traffic (egress) is restricted, this PoC uses **Error-Based Exfiltration** to return command results directly through the Flink Web UI.

## Features
- **Privilege Escalation Ready**: Demonstrated RCE with `root` privileges.
- **Egress Bypass**: No need for reverse shells or external DNS/HTTP; results are exfiltrated via Java Stacktraces.
- **Operational Stability**: Uses `NoRestartStrategy` to prevent infinite execution loops upon failure.
- **Dynamic Command Injection**: Accepts arbitrary shell commands via Flinks `Program Arguments`.

## Installation & Build

1. Ensure you have Maven and JDK 11+ installed.
2. Build the "fat" JAR:
   ```bash
   mvn clean package

```

3. The resulting artifact will be located in `target/flink-rce-1.0.jar`.

## Usage

1. Access the Flink Dashboard (typically on port `8081`).
2. Navigate to **Submit New Job** -> **Add New**.
3. Upload `flink-rce-1.0.jar`.
4. In the **Program Arguments** field, enter your desired shell command:
```bash
id; uname -a; env; ls -la /var/run/secrets/kubernetes.io/serviceaccount/

```

5. Click **Submit**.
6. The job will fail intentionally. Navigate to the **Exceptions** tab of the failed job.
7. Locate the `COMMAND RESULT` block within the stacktrace to view the output.

## Technical Details

* **Execution Context**: The command is executed via `ProcessBuilder` with `redirectErrorStream(true)` to capture both `stdout` and `stderr`.
* **Fault Tolerance**: Explicitly disables Flink default restart behavior to ensure clean, one-time execution.

## Legal Disclaimer

This tool is for legal authorized security auditing and penetration testing purposes only. Usage against systems without prior consent is illegal.
