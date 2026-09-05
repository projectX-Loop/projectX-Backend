---
name: commit
description: Create or amend Git commits for this repository, including writing commit messages that follow its local convention.
---

# Commit Messages

Use this skill before creating or amending a Git commit. `AGENTS.md` at the
repository root is the sole source of truth for commit-message conventions.

Before writing the message, inspect the staged diff and relevant recent commits
so the subject accurately describes the change being committed. Keep unrelated
changes out of the commit when they can be separated safely.

When using `git commit` or `git commit --amend`, follow the commit-message
format in `AGENTS.md`. Do not reuse pull-request title conventions for commit
messages.
