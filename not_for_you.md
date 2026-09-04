# not_for_you.md

A personal working log. Not documentation, and nothing here is needed to use or contribute to FeedStack. Everything a newcomer actually needs is in [README.md](./README.md) and [DEVDOC.md](./DEVDOC.md).

---

## The summariser was the worst thing found in this whole pass

Clicking **Article Summary** rendered this into the article body, in the browser, for the user:

```
Summary - Requirement already satisfied: pip in ./se_p2_env/lib/python3.12/site-packages ...
ERROR: Could not open requirements file: [Errno 2] No such file or directory: 'requirements.txt'
... externally-managed-environment ... Traceback (most recent call last):
  File "/home/cherry/Documents/My_Projects/FeedStack/reader-web/summarizer.py", line 22
    from groq import Groq
ModuleNotFoundError: No module named 'groq'
```

Three separate problems stacked on each other.

### `summarizer.py` installed software when a user asked for a summary

The script's import guard did not just report a missing dependency. On `ImportError` it created a virtualenv, ran `pip install --upgrade pip`, ran `pip install -r requirements.txt`, and if creating the venv failed it ran:

```python
subprocess.run(['sudo', 'apt', 'install', '-y', 'python3.10-venv'], check=True)
```

A web request, from any logged-in user, triggering `sudo apt install`. Even setting the sudo aside, it fetches and executes code from the network at request time, on a thread serving an HTTP request, every time the dependency is absent.

Removed entirely. The script now reports the missing dependency on stderr and exits non-zero. Dependencies are the operator's job and `requirements.txt` already lists them.

### The Java bridge returned the whole process output as the summary

```java
pb.redirectErrorStream(true);          // stderr merged into stdout
...
return "Summary - \n" + output.toString().trim();   // whatever it printed
```

So the pip transcript, the ANSI colour codes and the traceback (**including the absolute path of the script on the server**) became the article summary. That is an information disclosure: any reader learns the deployment directory layout.

There was also no exit-code check and no timeout, so a script that hung held the request thread indefinitely.

Rewritten: stderr is kept separate and logged, the exit code is checked, there is a 60 second timeout, and a failure returns a fixed `"Summary unavailable."` string. Nothing about the server can reach the browser now.

Verified both ways round: before, the page contained `/home/cherry/...` and `Traceback`; after, it contains neither and shows `Summary unavailable.` instead.

### And the escaping was wrong anyway

The old call did `text.replace("\"", "\\\"")` before handing the text to `ProcessBuilder`. `ProcessBuilder` passes arguments straight to `execve` without a shell, so there is nothing to escape; the backslashes were being inserted **into the article text** the model was asked to summarise. Removed.

## The feature has never worked

Worth being plain about: `groq` is not installed, `tokens.env` is not present, so the summariser has presumably never produced a summary on any machine but the author's. The fixes above make it fail safely rather than make it work. Getting it working needs `pip install -r requirements.txt` and a Groq API key.

No key was ever committed. `tokens.env.example` is a placeholder, `tokens.env` is gitignored, and a full scan of every blob in history turns up no `gsk_` token. The `SECRETS.md` finding for this repo (an `API_KEY` in `ContentUrlStrategy.java` and a `pk_live_` string in an HTML fixture) is inherited from upstream sismics/reader and is not the author's to rotate.

## There was no CI

A Maven project with 24 tests and nothing running them. The only workflow was a manual "Automated Code Refactoring" job that force-pushes a branch.

Added a build-and-test workflow on Temurin **8**, which is what `maven.compiler.source`/`target` actually pin, rather than whatever the runner ships. Verified on the runner: build succeeds, 24 tests run, 3 skipped, 0 failures.

## Notes

- The local machine had no JDK, so `openjdk-8-jdk` and `maven` were installed to run the app. That is a persistent change to this machine, unlike the Docker containers used elsewhere in this pass, which were removed afterwards.
- The app is at `/reader-web/src/index.html`, not `/reader-web/`. The context root serves a directory listing in the `jetty:run` layout.
- No frame-blocking headers, so the capture harness could sit beside `index.html` and be framed directly. No proxy needed here.
- The first-run wizard blocks the UI until both the admin password is changed **and** `first_connection` is set false. Changing only the password is not enough.
- Themes are a server-side user setting (`POST /api/user` with `theme=dark`), not a client toggle, so switching theme for a capture is an API call and a reload.
- The app fetches real feeds on startup. Every screenshot here contains live third-party headlines from the day it was taken.

## Open threads

- **The UI still says "Sismics Reader"** in the page title and the installation wizard, while everything else calls it FeedStack. Not wrong, given the fork, but inconsistent.
- The summariser needs its dependencies and a key before it does anything.
- `usertests`-style coverage of the new part-two features (summary, daily report, duplicate detector) does not exist; the 24 tests are inherited and cover the reader core.
- The Android client is a separate Gradle project and is not built by CI at all.
