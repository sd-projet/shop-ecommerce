export type Role = "ROLE_CUSTOMER" | "ROLE_ADMIN";
export type OrderStatus = "PENDING" | "PAID" | "SHIPPED" | "DELIVERED" | "CANCELLED";
export type ShippingMethod = "STANDARD" | "EXPRESS" | "PICKUP";
export type ProductSort = "NEWEST" | "PRICE_ASC" | "PRICE_DESC" | "POPULARITY";

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface Product {
  id: string;
  name: string;
  description: string | null;
  priceCents: number;
  imageUrl: string | null;
  category: string | null;
  stock: number;
  salesCount: number;
  archived: boolean;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Address {
  fullName: string;
  street: string;
  city: string;
  postalCode: string;
  country: string;
}

export interface OrderItem {
  productId: string;
  productName: string;
  unitPriceCents: number;
  quantity: number;
}

export interface Order {
  id: string;
  userId: string;
  status: OrderStatus;
  items: OrderItem[];
  address: Address;
  shippingMethod: ShippingMethod;
  itemsTotalCents: number;
  shippingCents: number;
  totalCents: number;
  createdAt: string;
  updatedAt: string;
}

export interface CheckoutResponse {
  order: Order;
  clientSecret: string;
}

export interface Dashboard {
  revenueCents: number;
  totalOrders: number;
  ordersByStatus: Record<OrderStatus, number>;
  activeProducts: number;
  lowStockProducts: number;
  customers: number;
}
