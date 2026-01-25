import { HttpParams } from '@angular/common/http';

export type QueryParams = Record<
  string,
  string | number | boolean | Array<string | number> | null | undefined
>;

export function buildHttpParams(params: QueryParams): HttpParams {
  let httpParams = new HttpParams();

  for (const [key, value] of Object.entries(params)) {
    if (value == null) continue;

    if (Array.isArray(value)) {
      for (const item of value) {
        if (item == null || item === '') continue;
        httpParams = httpParams.append(key, String(item));
      }
      continue;
    }

    if (typeof value === 'string') {
      const trimmed = key === 'q' ? value.trim() : value;
      if (trimmed === '') continue;
      httpParams = httpParams.set(key, trimmed);
      continue;
    }

    httpParams = httpParams.set(key, String(value));
  }

  return httpParams;
}







