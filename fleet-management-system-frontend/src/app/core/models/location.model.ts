export interface LocationDTO {
  id: number;
  street?: string | null;
  buildingNumber?: string | null;
  city?: string | null;
  postcode?: string | null;
  country?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  usedAsPickup?: boolean;
  usedAsDelivery?: boolean;
  usedInTransport?: boolean;
}
export interface CreateLocationRequest  {
  street?: string | null;
  buildingNumber?: string | null;
  city?: string | null;
  postcode?: string | null;
  country?: string | null;
  latitude?: number | null;
  longitude?: number | null;
}



