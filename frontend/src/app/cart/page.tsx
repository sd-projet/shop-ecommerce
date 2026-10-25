"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { formatPrice } from "@/lib/format";
import { cartTotal, useCart } from "@/store/cart";

export default function CartPage() {
  const { items, setQuantity, remove, clear } = useCart();
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);
  if (!mounted) return null;

  if (items.length === 0) {
    return (
      <div className="py-16 text-center">
        <p className="mb-4 text-slate-600">Votre panier est vide.</p>
        <Link href="/" className="btn-primary">Parcourir le catalogue</Link>
      </div>
    );
  }

  return (
    <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
      <div className="card divide-y divide-slate-100">
        {items.map((item) => (
          <div key={item.productId} className="flex items-center gap-4 p-4">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src={item.imageUrl ?? ""} alt={item.name} className="h-20 w-20 rounded-lg object-cover" />
            <div className="flex-1">
              <Link href={`/products/${item.productId}`} className="font-semibold hover:text-brand-600">{item.name}</Link>
              <p className="text-sm text-slate-500">{formatPrice(item.priceCents)}</p>
            </div>
            <input
              className="input w-20"
              type="number"
              min={1}
              max={item.stock}
              value={item.quantity}
              onChange={(e) => setQuantity(item.productId, Number(e.target.value) || 1)}
            />
            <p className="w-24 text-right font-semibold">{formatPrice(item.priceCents * item.quantity)}</p>
            <button className="text-sm text-red-600 hover:underline" onClick={() => remove(item.productId)}>Retirer</button>
          </div>
        ))}
      </div>
      <aside className="card h-fit space-y-4 p-5">
        <h2 className="text-lg font-bold">Récapitulatif</h2>
        <div className="flex justify-between"><span>Sous-total</span><span className="font-semibold">{formatPrice(cartTotal(items))}</span></div>
        <p className="text-xs text-slate-500">Les frais de livraison sont calculés à l&apos;étape suivante.</p>
        <Link href="/checkout" className="btn-primary w-full">Passer la commande</Link>
        <button className="btn-secondary w-full" onClick={clear}>Vider le panier</button>
      </aside>
    </div>
  );
}
