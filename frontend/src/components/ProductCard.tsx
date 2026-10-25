"use client";

import Link from "next/link";
import { formatPrice } from "@/lib/format";
import type { Product } from "@/lib/types";
import { useCart } from "@/store/cart";

export function ProductCard({ product }: { product: Product }) {
  const add = useCart((s) => s.add);
  const soldOut = product.stock === 0;
  return (
    <div className="card group flex flex-col overflow-hidden transition hover:shadow-md">
      <Link href={`/products/${product.id}`} className="overflow-hidden">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src={product.imageUrl ?? "/products/cable.svg"} alt={product.name} className="aspect-square w-full object-cover transition duration-300 group-hover:scale-105" />
      </Link>
      <div className="flex flex-1 flex-col gap-2 p-4">
        <span className="text-xs font-medium uppercase tracking-wide text-brand-600">{product.category}</span>
        <Link href={`/products/${product.id}`} className="font-semibold hover:text-brand-600">
          {product.name}
        </Link>
        <div className="mt-auto flex items-center justify-between pt-2">
          <span className="text-lg font-bold">{formatPrice(product.priceCents)}</span>
          {soldOut ? (
            <span className="text-sm font-medium text-red-600">Rupture</span>
          ) : (
            <button className="btn-primary" onClick={() => add(product)}>
              Ajouter
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
