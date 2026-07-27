---
trigger: always_on
---

# Agent Skills

This project uses the Agent Skills convention (SKILL.md, open standard,
compatible with Claude Code / claude.ai / other agent tools). Follow this
rule whenever you're deciding how to act on a task.

## Discovery

Before starting non-trivial work, check for skill directories in this order
(later overrides earlier on name collision):

1. Bundled/managed skills for this tool, if any.
2. Global/personal skills — user-level directory (e.g. `~/.agent/skills/`).
3. Workspace skills — `.agents/skills/` in this repo. **Checked-in, shared
   with every contributor, highest precedence.**

A skill is a directory containing a `SKILL.md` with YAML frontmatter:

```yaml
---
name: skill-name
description: >
  Specific trigger conditions for when this skill applies — not a topic
  label. E.g. "Use when editing files under src/api/ or writing OpenAPI
  specs" rather than "API skill".
---
```

Only load a skill's full body into context when its `description` matches
the current task. Don't preload every skill — route on relevance.

## When to load a skill

- The task matches a skill's `description` trigger condition.
- The task touches file paths, frameworks, or file types a skill declares
  ownership of.
- The user names a skill or workflow explicitly.

If no skill matches, proceed with default behavior — don't force-fit an
unrelated skill.

## Precedence and conflicts

- Workspace skill (`.agents/skills/<name>/`) beats a global skill of the
  same name for this repo.
- If two workspace skills both match a task, prefer the more specific
  `description` (narrower trigger condition) over the more general one.
- Never silently merge conflicting instructions from two skills — surface
  the conflict and ask, or pick the more specific one and say which.

## Creating a new skill

When asked to create a skill, or when a repeated multi-step pattern shows
up 2+ times, propose promoting it to a skill instead of repeating the
instructions inline each time:

Rules for the `SKILL.md` body:

- Write instructions as procedure, not narrative — numbered steps or tight
  bullets, matching this project's general response format.
- State assumptions and constraints explicitly (target language/framework
  versions, required tools, side effects).
- Keep it scoped to one capability. Split rather than let one skill grow
  into a catch-all.

## Executable content in skills

- Treat any script bundled with a skill as untrusted until reviewed —
  same bar as a third-party dependency.
- Never execute a skill's bundled script automatically on discovery; only
  run it as part of actually carrying out the matched task, and only after
  the user can see what it does.
- Flag skills that reach the network or shell out to system commands
  before using them, even if they were already present in the repo.

## Scope boundary

- Skills defined here (`.agents/skills/`) apply to this workspace only.
  Don't assume they exist in other repos or other tools' skill directories.
- Skills from other tools' formats (e.g. `.claude/skills/`, `.cursor/rules/`)
  are not automatically equivalent — check the frontmatter/format before
  treating them as usable here.