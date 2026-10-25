"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";
import { Pagination } from "@/components/Pagination";
import { ProductCard } from "@/components/ProductCard";
import { api } from "@/lib/api";
import type { Page, Product, ProductSort } from "@/lib/types";

function useDebounced<T>(value: T, delay = 350) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return debounced;
}

export default function CatalogPage() {
  return (
    <Suspense fallback={<p>Chargement…</p>}>
      <Catalog />
    </Suspense>
  );
}

function Catalog() {
  // the navbar search box navigates to /?q=...
  const urlQuery = useSearchParams().get("q") ?? "";
  const [search, setSearch] = useState(urlQuery);
  useEffect(() => setSearch(urlQuery), [urlQuery]);
  const [category, setCategory] = useState("");
  const [sort, setSort] = useState<ProductSort>("NEWEST");
  const [inStock, setInStock] = useState(false);
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [page, setPage] = useState(0);

  const q = useDebounced(search);
  const min = useDebounced(minPrice);
  const max = useDebounced(maxPrice);

  useEffect(() => setPage(0), [q, category, sort, inStock, min, max]);

  const categories = useQuery({ queryKey: ["categories"], queryFn: () => api<string[]>("/api/categories") });
  const products = useQuery({
    queryKey: ["products", { q, category, sort, inStock, min, max, page }],
    queryFn: () =>
      api<Page<Product>>("/api/products", {
        params: {
          q,
          category,
          sort,
          inStock,
          page,
          size: 12,
          minPriceCents: min ? Math.round(Number(min) * 100) : undefined,
          maxPriceCents: max ? Math.round(Number(max) * 100) : undefined,
        },
      }),
    placeholderData: keepPreviousData,
  });

  return (
    <div className="grid gap-8 md:grid-cols-[240px_1fr]">
      <aside className="card h-fit space-y-4 p-4">
        <div>
          <label className="label">Recherche</label>
          <input className="input" placeholder="Nom, description, catégorie…" value={search} onChange={(e) => setSearch(e.target.value)} />
        </div>
        <div>
          <label className="label">Catégorie</label>
          <select className="input" value={category} onChange={(e) => setCategory(e.target.value)}>
            <option value="">Toutes</option>
            {categories.data?.map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label">Prix (€)</label>
          <div className="flex gap-2">
            <input className="input" type="number" min={0} placeholder="Min" value={minPrice} onChange={(e) => setMinPrice(e.target.value)} />
            <input className="input" type="number" min={0} placeholder="Max" value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)} />
          </div>
        </div>
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" checked={inStock} onChange={(e) => setInStock(e.target.checked)} />
          En stock uniquement
        </label>
      </aside>

      <section>
        <div className="mb-4 flex items-center justify-between">
          <p className="text-sm text-slate-500">{products.data ? `${products.data.totalElements} produit(s)` : "Chargement…"}</p>
          <select className="input w-auto" value={sort} onChange={(e) => setSort(e.target.value as ProductSort)}>
            <option value="NEWEST">Nouveautés</option>
            <option value="POPULARITY">Popularité</option>
            <option value="PRICE_ASC">Prix croissant</option>
            <option value="PRICE_DESC">Prix décroissant</option>
          </select>
        </div>
        {products.isError && <p className="text-red-600">Impossible de charger le catalogue.</p>}
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {products.data?.content.map((p) => <ProductCard key={p.id} product={p} />)}
        </div>
        {products.data?.content.length === 0 && <p className="mt-8 text-center text-slate-500">Aucun produit ne correspond à votre recherche.</p>}
        <Pagination page={page} totalPages={products.data?.totalPages ?? 0} onChange={setPage} />
      </section>
    </div>
  );
}
