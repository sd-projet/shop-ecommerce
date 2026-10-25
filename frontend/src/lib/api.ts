import { useAuth } from "@/store/auth";

export const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public fieldErrors: Record<string, string> = {},
  ) {
    super(message);
  }
}

interface Options {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  params?: Record<string, string | number | boolean | undefined | null>;
}

export async function api<T>(path: string, { method = "GET", body, params }: Options = {}): Promise<T> {
  const url = new URL(path, API_URL);
  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined && value !== null && value !== "") url.searchParams.set(key, String(value));
    }
  }
  const token = useAuth.getState().token;
  const response = await fetch(url, {
    method,
    headers: {
      ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (response.status === 401 && token) {
    // expired / invalid token: drop the session
    useAuth.getState().logout();
  }
  if (!response.ok) {
    let message = response.statusText;
    let fieldErrors: Record<string, string> = {};
    try {
      const data = await response.json();
      message = data.message ?? message;
      fieldErrors = data.fieldErrors ?? {};
    } catch {
      /* no body */
    }
    throw new ApiError(response.status, message, fieldErrors);
  }
  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}
