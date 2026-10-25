"use client";

import { useQuery } from "@tanstack/react-query";
import { useParams } from "next/navigation";
import { useState } from "react";
import { api } from "@/lib/api";
import { formatPrice } from "@/lib/format";
import type { Product } from "@/lib/types";
import { useCart } from "@/store/cart";

export default function ProductPage() {
  const { id } = useParams<{ id: string }>();
  const add = useCart((s) => s.add);
  const [quantity, setQuantity] = useState(1);
  const [added, setAdded] = useState(false);
  const { data: product, isLoading, isError } = useQuery({ queryKey: ["product", id], queryFn: () => api<Product>(`/api/products/${id}`) });

  if (isLoading) return <p>Chargement…</p>;
  if (isError || !product) return <p className="text-red-600">Produit introuvable.</p>;

  return (
    <div className="grid gap-8 md:grid-cols-2">
      {/* eslint-disable-next-line @next/next/no-img-element */}
      <img src={product.imageUrl ?? ""} alt={product.name} className="card aspect-square w-full object-cover" />
      <div className="space-y-4">
        <span className="text-xs font-medium uppercase tracking-wide text-brand-600">{product.category}</span>
        <h1 className="text-3xl font-bold">{product.name}</h1>
        <p className="text-2xl font-bold">{formatPrice(product.priceCents)}</p>
        <p className="text-slate-600">{product.description}</p>
        <p className={product.stock > 0 ? "text-sm font-medium text-emerald-700" : "text-sm font-medium text-red-600"}>
          {product.stock === 0 ? "Rupture de stock" : product.stock <= 5 ? `Plus que ${product.stock} en stock` : `${product.stock} en stock`}
        </p>
        {product.stock > 0 && (
          <div className="flex items-center gap-3">
            <input
              className="input w-24"
              type="number"
              min={1}
              max={product.stock}
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, Math.min(product.stock, Number(e.target.value) || 1)))}
            />
            <button
              className="btn-primary"
              onClick={() => {
                add(product, quantity);
                setAdded(true);
                setTimeout(() => setAdded(false), 2000);
              }}
            >
              {added ? "Ajouté ✓" : "Ajouter au panier"}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
