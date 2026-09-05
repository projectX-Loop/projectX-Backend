---
name: commit
description: Create or amend Git commits for this repository, including writing commit messages that follow its local convention.
---

# Meaningful Commits

Use this skill before creating or amending a Git commit. `AGENTS.md` at the
repository root is the sole source of truth for commit-message conventions.

Create commits from cohesive changes, not from the entire working tree by
default. A commit should represent one independently understandable behavior,
fix, migration, or configuration change that can be reviewed and reverted as a
unit.

## Workflow

1. Inspect `git status`, the full unstaged/staged diff, and recent commits.
   Identify the smallest meaningful change sets and preserve pre-existing user
   changes that are outside the requested work.
2. Stage one change set at a time. Prefer explicit paths when a file belongs
   wholly to that change; use `git add -p` when one file contains separable
   changes. Do not use `git add .` or `git add -A` unless the inspected working
   tree contains only the one intended change set.
3. Before each commit, inspect `git diff --cached --check` and the staged diff.
   Confirm that the staged changes are self-contained, match the commit's
   intent, and contain no accidental files or unrelated formatting.
4. Run the smallest relevant verification for that change set before committing
   when practical. If verification must cover multiple commits together, run it
   before the final commit and record that scope accurately.
5. Write a concise English imperative subject that describes only the staged
   change. Follow the commit-message convention in `AGENTS.md`; do not reuse
   pull-request title conventions.
6. Repeat from staging for each remaining change set. After the final commit,
   inspect `git status` and report any intentionally uncommitted user changes.

Do not split tightly coupled code and its required migration, configuration, or
test solely to increase commit count. Prefer the fewest commits that preserve
clear review and rollback boundaries.
