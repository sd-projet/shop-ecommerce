"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { AuthResponse } from "@/lib/types";
import { useAuth } from "@/store/auth";

export default function RegisterPage() {
  const router = useRouter();
  const setSession = useAuth((s) => s.setSession);
  const [form, setForm] = useState({ firstName: "", lastName: "", email: "", password: "" });
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) => setForm({ ...form, [key]: e.target.value });

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setFieldErrors({});
    try {
      const res = await api<AuthResponse>("/api/auth/register", { method: "POST", body: form });
      setSession(res.token, res.user);
      router.push("/");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
        setFieldErrors(err.fieldErrors);
      } else setError("Inscription impossible");
    } finally {
      setLoading(false);
    }
  }

  const field = (key: keyof typeof form, label: string, type = "text") => (
    <div>
      <label className="label">{label}</label>
      <input className="input" type={type} required value={form[key]} onChange={set(key)} />
      {fieldErrors[key] && <p className="mt-1 text-xs text-red-600">{fieldErrors[key]}</p>}
    </div>
  );

  return (
    <form onSubmit={submit} className="card mx-auto max-w-md space-y-4 p-6">
      <h1 className="text-2xl font-bold">Créer un compte</h1>
      {error && <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}
      <div className="grid grid-cols-2 gap-3">
        {field("firstName", "Prénom")}
        {field("lastName", "Nom")}
      </div>
      {field("email", "E-mail", "email")}
      {field("password", "Mot de passe (8 caractères min.)", "password")}
      <button className="btn-primary w-full" disabled={loading}>{loading ? "Création…" : "S'inscrire"}</button>
      <p className="text-center text-sm text-slate-500">
        Déjà inscrit ? <Link href="/login" className="font-medium text-brand-600">Connexion</Link>
      </p>
    </form>
  );
}
