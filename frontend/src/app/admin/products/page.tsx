"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Pagination } from "@/components/Pagination";
import { api, ApiError } from "@/lib/api";
import { formatPrice } from "@/lib/format";
import type { Page, Product } from "@/lib/types";

interface FormState {
  id?: string;
  name: string;
  description: string;
  price: string;
  imageUrl: string;
  category: string;
  stock: string;
}

const EMPTY: FormState = { name: "", description: "", price: "", imageUrl: "", category: "", stock: "0" };

export default function AdminProducts() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState<FormState | null>(null);
  const [error, setError] = useState<string | null>(null);

  const products = useQuery({
    queryKey: ["admin", "products", page, search],
    queryFn: () => api<Page<Product>>("/api/admin/products", { params: { page, size: 15, q: search } }),
  });
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["admin"] }).then(() => queryClient.invalidateQueries({ queryKey: ["products"] }));

  const save = useMutation({
    mutationFn: (f: FormState) => {
      const body = {
        name: f.name,
        description: f.description,
        priceCents: Math.round(Number(f.price) * 100),
        imageUrl: f.imageUrl,
        category: f.category,
        stock: Number(f.stock),
      };
      return f.id ? api<Product>(`/api/admin/products/${f.id}`, { method: "PUT", body }) : api<Product>("/api/admin/products", { method: "POST", body });
    },
    onSuccess: () => {
      setForm(null);
      setError(null);
      invalidate();
    },
    onError: (e) => setError(e instanceof ApiError ? e.message : "Erreur"),
  });
  const updateStock = useMutation({
    mutationFn: ({ id, stock }: { id: string; stock: number }) => api<Product>(`/api/admin/products/${id}/stock`, { method: "PATCH", body: { stock } }),
    onSuccess: invalidate,
  });
  const archive = useMutation({ mutationFn: (id: string) => api<void>(`/api/admin/products/${id}`, { method: "DELETE" }), onSuccess: invalidate });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <input className="input max-w-xs" placeholder="Rechercher un produit…" value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }} />
        <button className="btn-primary" onClick={() => setForm(EMPTY)}>+ Nouveau produit</button>
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              <th className="p-3">Produit</th><th className="p-3">Catégorie</th><th className="p-3">Prix</th><th className="p-3">Stock</th><th className="p-3">Ventes</th><th className="p-3" />
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {products.data?.content.map((p) => (
              <tr key={p.id} className={p.archived ? "bg-slate-50 text-slate-400" : ""}>
                <td className="p-3 font-medium">{p.name}{p.archived && " (archivé)"}</td>
                <td className="p-3">{p.category}</td>
                <td className="p-3">{formatPrice(p.priceCents)}</td>
                <td className="p-3">
                  <input
                    key={`${p.id}-${p.stock}`}
                    className={`input w-20 ${p.stock <= 5 ? "border-red-300" : ""}`}
                    type="number"
                    min={0}
                    defaultValue={p.stock}
                    disabled={p.archived}
                    onBlur={(e) => {
                      const value = Number(e.target.value);
                      if (value !== p.stock && value >= 0) updateStock.mutate({ id: p.id, stock: value });
                    }}
                  />
                </td>
                <td className="p-3">{p.salesCount}</td>
                <td className="space-x-3 p-3 text-right">
                  {!p.archived && (
                    <>
                      <button className="text-brand-600 hover:underline" onClick={() => setForm({ id: p.id, name: p.name, description: p.description ?? "", price: String(p.priceCents / 100), imageUrl: p.imageUrl ?? "", category: p.category ?? "", stock: String(p.stock) })}>
                        Modifier
                      </button>
                      <button className="text-red-600 hover:underline" onClick={() => confirm(`Archiver « ${p.name} » ?`) && archive.mutate(p.id)}>Archiver</button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <Pagination page={page} totalPages={products.data?.totalPages ?? 0} onChange={setPage} />

      {form && (
        <div className="fixed inset-0 z-20 flex items-center justify-center bg-black/40 p-4">
          <form
            className="card max-h-[90vh] w-full max-w-lg space-y-3 overflow-y-auto p-6"
            onSubmit={(e) => {
              e.preventDefault();
              save.mutate(form);
            }}
          >
            <h2 className="text-lg font-bold">{form.id ? "Modifier le produit" : "Nouveau produit"}</h2>
            {error && <p className="rounded-lg bg-red-50 p-2 text-sm text-red-700">{error}</p>}
            <div><label className="label">Nom</label><input className="input" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></div>
            <div><label className="label">Description</label><textarea className="input" rows={3} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
            <div className="grid grid-cols-2 gap-3">
              <div><label className="label">Prix (€)</label><input className="input" type="number" step="0.01" min={0} required value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} /></div>
              <div><label className="label">Stock</label><input className="input" type="number" min={0} required value={form.stock} onChange={(e) => setForm({ ...form, stock: e.target.value })} /></div>
            </div>
            <div><label className="label">Catégorie</label><input className="input" required value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} /></div>
            <div><label className="label">URL de l&apos;image</label><input className="input" value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} /></div>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" className="btn-secondary" onClick={() => { setForm(null); setError(null); }}>Annuler</button>
              <button className="btn-primary" disabled={save.isPending}>Enregistrer</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
