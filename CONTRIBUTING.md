# Contributing to TravelAI

TravelAI uses GitHub Flow: keep `main` stable, create a focused branch for
each task, and merge through a reviewed pull request.

## Branch Naming

Use a short prefix and a descriptive kebab-case name.

| Prefix | Use |
| --- | --- |
| `feat/` | New application behavior |
| `fix/` | Bug fixes |
| `docs/` | Documentation changes |
| `chore/` | Repository maintenance |
| `test/` | Test-only changes |

Examples:

```text
feat/landmark-history
fix/map-empty-state
docs/week3-git-workflow
chore/github-templates
```

## Commit Messages

Use Conventional Commits with a short imperative summary:

```text
<type>: <summary>
```

Preferred types are `feat`, `fix`, `docs`, `test`, `refactor`, `style`, and
`chore`.

Good:

```text
feat: add landmark scan history
fix: handle missing Maps API key
docs: document project setup
```

Avoid vague summaries such as `update code`, `fix stuff`, or `final`.

## Pull Request Process

1. Sync with `main` before opening a pull request.
2. Keep the pull request focused on one issue.
3. Link the issue with `Closes #<issue-number>` when the pull request completes
   the task.
4. Complete the validation checklist in the pull-request description.
5. Request at least one teammate review.
6. Address review comments before merging.
7. Use the GitHub merge button. Do not force-push shared branches.

## Review Checklist

Reviewers check:

- architecture and ownership boundaries
- user-facing behavior and error handling
- security, especially secrets and API keys
- naming, readability, and focused scope
- tests or manual validation appropriate to the change

## Conflict Rules

- Pull or fetch before starting work and before opening a pull request.
- Prefer small pull requests and frequent merges.
- Resolve conflicts locally in Android Studio or VS Code.
- Re-run relevant checks after resolving a conflict.
- Ask the owner of the affected area to review the resolution.
- Never use `git push --force` on shared branches or `main`.

See [Git workflow](docs/GIT_WORKFLOW.md) for the team model and Week 3 demo
script.
