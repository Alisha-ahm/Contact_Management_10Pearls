/**
 * Spring Boot REST API Client
 * Connects React frontend to Java Spring Boot backend (/api/v1/*) when VITE_USE_BACKEND_API=true.
 */

import { Contact, User } from '../types';

const API_BASE = import.meta.env.VITE_API_BASE_URL || '';
const TOKEN_KEY = 'cms_jwt_token';

export const isBackendApiEnabled = (): boolean =>
  import.meta.env.VITE_USE_BACKEND_API === 'true';

export function getAuthToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setAuthToken(token: string | null) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getAuthToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errBody = await response.json().catch(() => ({}));
    throw new Error(errBody.message || `Request failed with status ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const backendApi = {
  register: (payload: { fullName: string; identifier: string; password: string; company?: string }) =>
    apiRequest<{ token: string; message: string; user: User }>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  login: (payload: { identifier: string; password: string }) =>
    apiRequest<{ token: string; message: string; user: User }>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  getProfile: () => apiRequest<User>('/api/v1/users/me'),

  updateProfile: (payload: { fullName: string; phone?: string; company?: string }) =>
    apiRequest<User>('/api/v1/users/me', {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),

  changePassword: (payload: { currentPassword: string; newPassword: string }) =>
    apiRequest<{ message: string }>('/api/v1/users/change-password', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  listContacts: (params: {
    search?: string;
    category?: string;
    favoriteOnly?: boolean;
    page: number;
    size: number;
    sortBy: string;
    sortDir: string;
  }) => {
    const qs = new URLSearchParams({
      search: params.search || '',
      category: params.category || 'All',
      favoriteOnly: String(Boolean(params.favoriteOnly)),
      page: String(Math.max(0, params.page - 1)),
      size: String(params.size),
      sortBy: params.sortBy,
      sortDir: params.sortDir,
    });
    return apiRequest<{
      contacts: Contact[];
      page: number;
      pageSize: number;
      totalItems: number;
      totalPages: number;
      totalAll: number;
      totalFavorites: number;
    }>(`/api/v1/contacts?${qs.toString()}`);
  },

  createContact: (payload: Omit<Contact, 'id' | 'userId' | 'createdAt' | 'updatedAt'>) =>
    apiRequest<Contact>('/api/v1/contacts', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  updateContact: (id: string, payload: Omit<Contact, 'id' | 'userId' | 'createdAt' | 'updatedAt'>) =>
    apiRequest<Contact>(`/api/v1/contacts/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),

  toggleFavorite: (id: string) =>
    apiRequest<{ isFavorite: boolean }>(`/api/v1/contacts/${id}/favorite`, {
      method: 'PATCH',
    }),

  deleteContact: (id: string) =>
    apiRequest<void>(`/api/v1/contacts/${id}`, {
      method: 'DELETE',
    }),

  bulkDelete: (ids: string[]) =>
    apiRequest<{ deletedCount: number }>('/api/v1/contacts/bulk-delete', {
      method: 'POST',
      body: JSON.stringify(ids),
    }),
};
