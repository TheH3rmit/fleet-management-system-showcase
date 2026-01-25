import { test, expect, type Page } from '@playwright/test';

type Transport = {
  id: number;
  status: string;
  plannedStartAt?: string | null;
  plannedEndAt?: string | null;
  plannedDistanceKm?: number | null;
  driverId?: number | null;
  vehicleId?: number | null;
  trailerId?: number | null;
  pickupLocationId?: number | null;
  deliveryLocationId?: number | null;
  vehicleLabel?: string | null;
  trailerLabel?: string | null;
};

type ApiState = {
  transports: Transport[];
  vehicles: any[];
  trailers: any[];
  locations: any[];
  drivers: any[];
  cargo: any[];
  timeline: any[];
  nextTransportId: number;
};

const creds = {
  dispatcher: {
    login: process.env['E2E_DISPATCHER_LOGIN'] ?? 'admin',
    password: process.env['E2E_DISPATCHER_PASSWORD'] ?? 'admin',
  },
  driver: {
    login: process.env['E2E_DRIVER_LOGIN'] ?? 'driver',
    password: process.env['E2E_DRIVER_PASSWORD'] ?? 'driver',
  },
};

function createDefaultState(): ApiState {
  return {
    transports: [
      {
        id: 101,
        status: 'PLANNED',
        plannedStartAt: '2026-01-10T08:00:00.000Z',
        plannedEndAt: '2026-01-10T16:00:00.000Z',
        plannedDistanceKm: 120,
        driverId: 10,
        vehicleId: 1,
        trailerId: 1,
        pickupLocationId: 1,
        deliveryLocationId: 2,
        vehicleLabel: 'WX12345 - Volvo FH16',
        trailerLabel: 'ABC-111 - Test1',
      }
    ],
    vehicles: [
      { id: 1, licensePlate: 'WX12345', manufacturer: 'Volvo', model: 'FH16', vehicleStatus: 'ACTIVE' }
    ],
    trailers: [
      { id: 1, name: 'Test1', licensePlate: 'ABC-111', trailerStatus: 'ACTIVE' }
    ],
    locations: [
      { id: 1, city: 'Warszawa', street: 'Magazynowa', buildingNumber: '10', postcode: '00-001', country: 'PL' },
      { id: 2, city: 'Gdansk', street: 'Portowa', buildingNumber: '3', postcode: '80-001', country: 'PL' }
    ],
    drivers: [
      { userId: 10, firstName: 'Jan', lastName: 'Kowalski', driverStatus: 'AVAILABLE' }
    ],
    cargo: [
      { id: 201, cargoDescription: 'Boxes', weightKg: 100, volumeM3: 2, transportId: 101 }
    ],
    timeline: [
      { id: 1, transportId: 101, status: 'PLANNED', changedAt: new Date().toISOString(), changedByUserId: 1 }
    ],
    nextTransportId: 200,
  };
}

async function mockApi(page: Page, state: ApiState) {
  await page.route('**/api/**', async (route) => {
    const req = route.request();
    const url = new URL(req.url());
    const path = url.pathname;
    const method = req.method();

    const json = (body: any, status = 200) =>
      route.fulfill({
        status,
        contentType: 'application/json',
        body: JSON.stringify(body),
      });

    if (path === '/api/transports' && method === 'GET') {
      const status = url.searchParams.get('status');
      const list = status
        ? state.transports.filter(t => t.status === status)
        : state.transports;
      return json({ content: list, totalElements: list.length });
    }

    if (path === '/api/transports' && method === 'POST') {
      const payload = JSON.parse(req.postData() ?? '{}');
      const created: Transport = {
        id: state.nextTransportId++,
        status: 'PLANNED',
        plannedStartAt: payload.plannedStartAt ?? null,
        plannedEndAt: payload.plannedEndAt ?? null,
        plannedDistanceKm: payload.plannedDistanceKm ?? null,
        driverId: payload.driverId ?? null,
        vehicleId: payload.vehicleId ?? null,
        trailerId: payload.trailerId ?? null,
        pickupLocationId: payload.pickupLocationId ?? null,
        deliveryLocationId: payload.deliveryLocationId ?? null,
      };
      state.transports.unshift(created);
      return json(created);
    }

    const statusMatch = path.match(/^\/api\/transports\/(\d+)\/status$/);
    if (statusMatch && method === 'PATCH') {
      const id = Number(statusMatch[1]);
      const payload = JSON.parse(req.postData() ?? '{}');
      const t = state.transports.find(x => x.id === id);
      if (!t) return json({ message: 'Not found' }, 404);
      t.status = payload.status;
      state.timeline.push({
        id: state.timeline.length + 1,
        transportId: id,
        status: payload.status,
        changedAt: new Date().toISOString(),
        changedByUserId: 1,
      });
      return json(t);
    }

    const historyMatch = path.match(/^\/api\/transports\/(\d+)\/history$/);
    if (historyMatch && method === 'GET') {
      const id = Number(historyMatch[1]);
      return json(state.timeline.filter(x => x.transportId === id));
    }

    if (path === '/api/vehicles' && method === 'GET') {
      if (url.searchParams.has('ids')) {
        const ids = (url.searchParams.get('ids') ?? '').split(',').map(Number);
        return json(state.vehicles.filter(v => ids.includes(v.id)));
      }
      return json({ content: state.vehicles, totalElements: state.vehicles.length });
    }

    if (path === '/api/vehicles/available' && method === 'GET') {
      return json(state.vehicles);
    }

    if (path === '/api/trailers' && method === 'GET') {
      return json({ content: state.trailers, totalElements: state.trailers.length });
    }

    if (path === '/api/trailers/available' && method === 'GET') {
      return json(state.trailers);
    }

    if (path === '/api/locations' && method === 'GET') {
      return json({ content: state.locations, totalElements: state.locations.length });
    }

    if (path === '/api/drivers' && method === 'GET') {
      if (url.searchParams.has('ids')) {
        const ids = (url.searchParams.get('ids') ?? '').split(',').map(Number);
        return json(state.drivers.filter(d => ids.includes(d.userId)));
      }
      return json({ content: state.drivers, totalElements: state.drivers.length });
    }

    if (path === '/api/drivers/available' && method === 'GET') {
      return json(state.drivers);
    }

    if (path === '/api/drivers/my-transports' && method === 'GET') {
      return json(state.transports);
    }

    if (path === '/api/drivers/my-cargo' && method === 'GET') {
      return json(state.cargo);
    }

    if (path === '/api/drivers/my-transports/timeline' && method === 'GET') {
      return json(state.timeline);
    }

    if (path === '/api/cargos' && method === 'GET') {
      return json({ content: state.cargo, totalElements: state.cargo.length });
    }

    return json({}, 200);
  });
}

async function login(page: Page, login: string, password: string) {
  await page.goto('/login');
  await page.locator('input[formcontrolname="login"]').fill(login);
  await page.locator('input[formcontrolname="password"]').fill(password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.locator('.error')).toHaveCount(0);
  await page.waitForURL('**/menu');
}

async function selectFirstOption(page: Page, label: string) {
  const field = page.locator('mat-form-field').filter({ hasText: label }).first();
  await field.scrollIntoViewIfNeeded();

  const input = field.locator('input').first();
  if (await input.count()) {
    await input.click({ force: true });
    await input.fill('a');
    await page.keyboard.press('Backspace');
  } else {
    const trigger = field.locator('.mat-mdc-select-trigger, mat-select').first();
    await trigger.click({ force: true });
  }

  await page.waitForSelector('mat-option', { state: 'visible', timeout: 10000 });
  const option = page.locator('mat-option:not([disabled])').first();
  await option.click();
}

test.describe.serial('Transport flows', () => {
  test('Login -> transports list -> filter status -> view details', async ({ page }) => {
    const state = createDefaultState();
    await mockApi(page, state);
    await login(page, creds.dispatcher.login, creds.dispatcher.password);

    await page.goto('/transports');
    await page.getByPlaceholder('Status, id, driver, vehicle, location...').fill('status: planned');

    const rows = page.locator('table tr.mat-mdc-row');
    await expect(rows.first()).toBeVisible({ timeout: 10000 });
    await expect(rows.first()).toContainText('PLANNED');

    const editButton = page.locator('table').locator('button').filter({
      has: page.locator('mat-icon', { hasText: 'edit' }),
    }).first();
    await editButton.click();

    await expect(page.getByRole('heading', { name: /Edit transport/i })).toBeVisible();

    await page.getByRole('button', { name: /Status history/i }).click();
    await expect(page.getByRole('heading', { name: 'Status history' })).toBeVisible();
  });

  test('Dispatcher: create transport -> assign driver/vehicle -> status flow', async ({ page }) => {
    const state = createDefaultState();
    await mockApi(page, state);
    await login(page, creds.dispatcher.login, creds.dispatcher.password);

    await page.goto('/transports');
    await page.getByRole('button', { name: /New Transport/i }).click();
    await expect(page.getByRole('heading', { name: /New transport/i })).toBeVisible();

    await page.getByLabel('Contractual due at').fill('2026-01-10T12:00');
    await page.getByLabel('Planned start').fill('2026-01-10T08:00');
    await page.getByLabel('Planned end').fill('2026-01-10T16:00');
    await page.getByLabel('Planned distance (km)').fill('120');

    await selectFirstOption(page, 'Vehicle');
    await selectFirstOption(page, 'Trailer');
    await selectFirstOption(page, 'Pickup location');
    await selectFirstOption(page, 'Delivery location');

    await selectFirstOption(page, 'Driver (optional)');

    await page.getByRole('button', { name: 'Create' }).click();

    const rows = page.locator('table tr.mat-mdc-row');
    await expect(rows.first()).toBeVisible({ timeout: 10000 });
    await expect(rows.first()).toContainText('PLANNED');
  });

  test('Driver: accept/start/finish transport -> timeline/cargo updates', async ({ page }) => {
    const state = createDefaultState();
    state.transports = [
      {
        id: 301,
        status: 'PLANNED',
        plannedStartAt: '2026-01-10T08:00:00.000Z',
        plannedEndAt: '2026-01-10T16:00:00.000Z',
        plannedDistanceKm: 120,
        driverId: 10,
        vehicleId: 1,
        trailerId: 1,
        pickupLocationId: 1,
        deliveryLocationId: 2,
        vehicleLabel: 'WX12345 - Volvo FH16',
        trailerLabel: 'ABC-111 - Test1',
      }
    ];
    state.cargo = [
      { id: 401, cargoDescription: 'Boxes', weightKg: 100, volumeM3: 2, transportId: 301 }
    ];
    await mockApi(page, state);
    await login(page, creds.driver.login, creds.driver.password);

    await page.goto('/drivers');

    const allTable = page.locator('table tr.mat-mdc-row').first();
    await expect(allTable).toBeVisible({ timeout: 10000 });

    await page.locator('button', { hasText: 'Accept' }).first().click();
    await expect(page.locator('table')).toContainText('ACCEPTED');

    await page.locator('button', { hasText: 'Start' }).first().click();
    await expect(page.locator('table')).toContainText('IN_PROGRESS');

    await page.locator('button', { hasText: 'Finish' }).first().click();
    await expect(page.locator('table')).toContainText('FINISHED');

    await page.getByRole('tab', { name: 'My timeline' }).click();
    await page.getByRole('button', { name: 'History' }).click();
    await selectFirstOption(page, 'Select transport');
    await page.getByRole('button', { name: /Refresh/i }).click();
    await expect(page.locator('table')).toContainText('FINISHED');

    await page.getByRole('tab', { name: 'My cargo' }).click();
    await page.getByRole('button', { name: 'All (read-only)' }).click();
    await expect(page.locator('.list-item').first()).toBeVisible();
  });
});
