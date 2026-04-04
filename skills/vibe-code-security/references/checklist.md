# Vibe-Code Security Checklist (printable)

## Secrets
- [ ] No secrets in frontend bundles (API keys, tokens, service-role keys)
- [ ] .env never committed; history checked; keys rotated if leaked

## Auth
- [ ] Server-side auth on every protected endpoint
- [ ] No frontend-only admin guards
- [ ] Ownership checks on every resource (IDOR prevention)

## Sessions
- [ ] No JWT in localStorage
- [ ] Tokens expire; refresh rotation
- [ ] Logout invalidates sessions server-side

## API hardening
- [ ] Rate limiting on login/nonce/claim endpoints
- [ ] Error responses are generic (no stack traces, no table names)
- [ ] CORS allowlist; no wildcard origins

## DB & infra
- [ ] Parameterized queries (no string concatenation)
- [ ] DB not publicly exposed; firewall/private network
- [ ] App runs as non-root
- [ ] HTTPS enforced

## Uploads
- [ ] Server-side MIME validation
- [ ] Block scripts/executables

## Supply chain
- [ ] `npm audit` in CI/CD
- [ ] Lockfiles committed and pinned

## Redirects
- [ ] Redirect URLs allowlisted (no open redirect)
