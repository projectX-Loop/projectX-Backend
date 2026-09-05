---
name: pr-writer
description: Create or edit GitHub pull request titles and bodies, including gh pr create and gh pr edit requests.
---

# PR Writer

Use this skill before creating or editing a GitHub pull request title or body.
`AGENTS.md` at the repository root is the sole source of truth for this
repository's PR title and body conventions.

## Required Checks

Before drafting, read `AGENTS.md`, inspect the relevant diff or commits, and
read `.github/PULL_REQUEST_TEMPLATE.md` when it exists. When a relevant
`.도윤/pr` planning document exists, read it for context only; do not copy it
verbatim.

Preserve every section from the PR template. If the template does not yet exist,
do not invent one: write a concise Korean body that reports the actual work,
validation, and useful review context.

## GitHub CLI

When using `gh pr create` or `gh pr edit`, apply the title and body conventions
in `AGENTS.md`. Do not derive the PR title from a scoped commit message.
