# Banking System React Frontend

This is a simple React frontend for the Banking System backend.

## Setup

1. Open a terminal in `frontend/`.
2. Run `npm install`.
3. Run `npm run dev`.

The frontend uses a Vite development server and proxies `/api` requests to `http://localhost:8080`.

## Features

- Register a new user
- Login and store JWT token
- Create accounts
- List accounts for the authenticated user

## Notes

- The backend must be running on port `8080`.
- If the backend is served from the same origin, proxying is not required and requests will work directly against `/api`.
