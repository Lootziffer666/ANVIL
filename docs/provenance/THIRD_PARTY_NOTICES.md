# THIRD_PARTY_NOTICES — Gate AT1

> Gate: AT1 — Source License & Provenance Lock  
> Status: `pending audit` — will be finalized when KEEP decisions are made in Gate AT3  
> Last updated: 2026-05-10

---

## Purpose

This file tracks attribution obligations for any code or substantial structure transplanted from donor codebases into Anvil.

**Rule:** Any KEEP decision in `TRANSPLANT_MAP.md` requires a corresponding entry here.  
**Rule:** This file must be kept in `docs/provenance/` — never in product UI or user-facing docs.

---

## Donor: ogcode

### License

```
MIT License

Copyright (c) 2026 Prasenjeet Symon

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Attribution Status

| Area | Transplant Decision | Attribution Required | Entry Added |
|------|---------------------|---------------------|-------------|
| *No KEEP decisions made yet* | — | — | — |

> This table will be populated when Gate AT3 (Transplant Map) produces KEEP decisions.  
> Until then, no code has been transplanted and no attribution entries are needed.

---

## Other Donors

*No other donor codebases registered.*

---

## Pending Audit Items

- [ ] Review each KEEP decision in `TRANSPLANT_MAP.md` for attribution requirements
- [ ] Verify no transitive dependency of a KEEP area introduces additional attribution
- [ ] Confirm all KEEP areas have entries in this file before code is merged to main

---

## Cross-References

- [`OGCODE_SOURCE_AUDIT.md`](OGCODE_SOURCE_AUDIT.md) — Full license audit
- [`LICENSE_DECISION.md`](LICENSE_DECISION.md) — Decision logic
- [`TRANSPLANT_MAP.md`](TRANSPLANT_MAP.md) — Keep / Rewrite / Drop decisions
