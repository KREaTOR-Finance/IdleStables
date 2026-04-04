---
name: vibe-code-security
description: Security audit checklist and remediation guidance for quickly built or AI/vibe-coded apps (React/Next, Node backends, Supabase, Vercel/Railway). Use to prevent common 24-hour hacks: leaked API keys, missing rate limits, SQL injection, wildcard CORS, JWT localStorage, weak secrets, frontend-only auth, .env leaks, verbose errors, unsafe file uploads, weak password hashing, non-expiring tokens, missing auth middleware, root servers, exposed DB ports, IDOR, missing HTTPS, logout sessions not invalidated, stale dependency audits, and open redirects. Includes a prioritized ship-blocker checklist and web3/game-specific notes (wallet auth, IDOR on resources, claim endpoints).
---

# Vibe-Code Security (ship-blocker checklist)

## Ground rules
- **Assume the frontend is hostile.** Anything in the browser can be read/modified.
- **Protect on the server** (or on-chain): authz, ownership checks, rate limits.
- **Treat every endpoint as public** until proven protected.

## 0) Ship blockers (do these before deploying)
1) **No secrets in frontend bundles** (no API keys, service role keys, tokens). If found → rotate.
2) **Rate limit** login/nonces/claim/critical endpoints.
3) **Authz on every route** (no frontend-only guards).
4) **IDOR audit**: every resource endpoint validates ownership server-side.
5) **No wildcard CORS**; allowlist origins.
6) **No tokens in localStorage**; prefer httpOnly cookies or short-lived signed nonces.
7) **No stack traces / internals** in responses.
8) **Dependency audit** in CI + pin lockfiles.

## 1) The 20 common “24-hour hack” issues (review + correct)
Use this as a scan list. If any are true, treat as exploitable.

1. **API keys in frontend JS** → move secrets server-side.
2. **No rate limiting on /login** → add rate limit + lockout after 5 fails.
3. **SQL via string concatenation** → parameterize queries.
4. **CORS = \"*\"** → allowlist origins.
5. **JWT in localStorage** → use httpOnly cookies.
6. **Weak JWT secret** → 256-bit random secret + rotation.
7. **Admin routes protected only in frontend** → enforce server-side.
8. **.env committed once** → rotate all keys immediately.
9. **Verbose error responses** → generic client errors; server logs only.
10. **File uploads without MIME validation** → server-side validation; block executable types.
11. **Passwords hashed with MD5/SHA1** → bcrypt/argon2.
12. **Auth tokens never expire** → expiries + refresh rotation.
13. **Missing auth middleware on internal routes** → audit endpoints.
14. **Server runs as root** → non-privileged runtime.
15. **DB port exposed publicly** → private network/firewall.
16. **IDOR** → ownership checks.
17. **No HTTPS enforcement** → force HTTPS + redirects.
18. **Logout doesn’t invalidate sessions** → server-side invalidation.
19. **npm packages not audited** → add audit step per deploy.
20. **Open redirect callbacks** → allowlist redirect destinations.

## 2) How to run a fast audit (15–30 minutes)
### Frontend
- Search for secrets:
  - `rg -n "apiKey|service_role|supabaseKey|secret|token" src/`
- Ensure no privileged keys exist client-side.

### Backend
- Enumerate all routes (print router table). Confirm each is protected.
- Add rate limits to: login, nonce, claim, create, upload.
- Confirm CORS allowlist and credentials behavior.

### DB
- Confirm DB has no public ingress.
- Confirm prepared statements/ORM parameterization.

### Session/auth
- Confirm expirations, rotation, and invalidation on logout.

### Dependencies
- `npm audit` / `pnpm audit` in CI.

## 3) Web3/game-specific additions (IdleStables-style)
- **Wallet auth**: require signed nonce for session; never trust wallet address from client.
- **Claim endpoints**: rate limit + verify ownership + replay protection.
- **Resource IDs** (horseId, raceId, trackId): prevent IDOR.
- **Admin/Steward keys**: store in platform secrets (Railway/Vercel) only; never in git.

## 4) Output format for audit results
When asked to audit a project, return:
- **Critical** (ship blockers) — numbered, with exact file/line if available
- **High** — exploitation likely
- **Medium** — defense-in-depth
- **Fix plan** — ordered steps + quick patches

## References
- See `references/checklist.md` for a printable checklist.
