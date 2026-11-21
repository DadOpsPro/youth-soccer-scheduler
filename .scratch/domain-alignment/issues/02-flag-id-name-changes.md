# Report a Coach or Field id that arrives with a different name

Status: needs-triage
Blocked by: 01

## What

When an import brings in a Coach or Field id that is already loaded from another Season, but with a different name, report it as an import problem. Don't guess which name is right, and never match on name alone.

## Why

ADR 0001: a Coach or Field is recognised across Seasons by a stable id that the Coordinator owns. A name mismatch usually means the ids weren't kept stable, and guessing would either miss a real Conflict or invent a false one.

## Done when

- Winter has `C1,Coach Rivera` and spring arrives with `C1,Coach Okafor`: the import reports the mismatch with its line number.
- Two different ids that share a name are treated as two people, with no warning.
- The same behaviour applies to Fields.
