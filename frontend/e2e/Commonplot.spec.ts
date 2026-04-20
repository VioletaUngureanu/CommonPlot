import { test, expect, type Page } from '@playwright/test'

// ============================================================
//  CommonPlot — Playwright E2E Tests
//  Scenariul 1: Register + Login flow
//  Scenariul 2: CRUD Meet-ups (add, edit, delete)
//  Scenariul 3: Validare formulare
// ============================================================

const clearSession = async (page: Page) => {
  await page.goto('/')
  await page.evaluate(() => sessionStorage.clear())
  await page.context().clearCookies()
}

const loginAsAdmin = async (page: Page) => {
  await page.goto('/login')
  await page.fill('input[placeholder="E-mail"]', 'admin@commonplot.com')
  await page.fill('input[placeholder="Password"]', 'admin123')
  await page.click('button:has-text("LOGIN")')
  await expect(page).toHaveURL(/\/meetups/)
}

// Selectează input-uri din form după index (evită probleme cu placeholder exact)
const fillRegisterForm = async (page: Page, data: {
  fullName: string, username: string, email: string, password: string, confirm: string
}) => {
  const inputs = page.locator('.auth-form input')
  await inputs.nth(0).fill(data.fullName)
  await inputs.nth(1).fill(data.username)
  await inputs.nth(2).fill(data.email)
  await inputs.nth(3).fill(data.password)
  await inputs.nth(4).fill(data.confirm)
}

// ════════════════════════════════════════════════════════════
//  SCENARIUL 1: Register + Login Flow
// ════════════════════════════════════════════════════════════
test.describe('Scenariul 1: Register + Login Flow', () => {

  test.beforeEach(async ({ page }) => {
    await clearSession(page)
  })

  test('1.1 — Utilizatorul se poate înregistra cu date valide', async ({ page }) => {
    await page.goto('/register')
    await fillRegisterForm(page, {
      fullName: 'Test User',
      username: 'testuser123',
      email:    'test@commonplot.com',
      password: 'password123',
      confirm:  'password123',
    })
    await page.click('button:has-text("REGISTER")')
    await expect(page).toHaveURL(/\/meetups/)
  })

  test('1.2 — Utilizatorul se poate loga cu contul admin existent', async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="E-mail"]', 'admin@commonplot.com')
    await page.fill('input[placeholder="Password"]', 'admin123')
    await page.click('button:has-text("LOGIN")')

    await expect(page).toHaveURL(/\/meetups/)
    await expect(page.locator('text=Meet-ups Management')).toBeVisible()
  })

  test('1.3 — Login cu date greșite afișează eroare', async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[placeholder="E-mail"]', 'wrong@email.com')
    await page.fill('input[placeholder="Password"]', 'wrongpassword')
    await page.click('button:has-text("LOGIN")')

    await expect(page.locator('text=Invalid email or password')).toBeVisible()
    await expect(page).toHaveURL(/\/login/)
  })

  test('1.4 — Userul logat este redirecționat de la /login la /meetups', async ({ page }) => {
    await loginAsAdmin(page)
    await page.goto('/login')
    await expect(page).toHaveURL(/\/meetups/)
  })

  test('1.5 — Pagina /meetups e inaccesibilă fără autentificare', async ({ page }) => {
    await page.goto('/meetups')
    await expect(page).toHaveURL(/\/login/)
  })
})

// ════════════════════════════════════════════════════════════
//  SCENARIUL 2: CRUD Meet-ups
// ════════════════════════════════════════════════════════════
test.describe('Scenariul 2: CRUD Meet-ups', () => {

  test.beforeEach(async ({ page }) => {
    await clearSession(page)
    await loginAsAdmin(page)
  })

  test('2.1 — Tabelul afișează meet-up-urile existente', async ({ page }) => {
    await expect(page.locator('table.meetup-table')).toBeVisible()
    const rows = page.locator('table.meetup-table tbody tr')
    await expect(rows).not.toHaveCount(0)
  })

  test('2.2 — Utilizatorul poate adăuga un meet-up nou', async ({ page }) => {
    await page.click('button:has-text("New Meet-up")')
    await expect(page.locator('.modal')).toBeVisible()

    // Folosim placeholder-ele exacte din MeetupModal.vue
    await page.locator('.modal').locator('input[placeholder="e.g. Morning Coffee & Dostoievski"]').fill('Playwright Test Meet-up')
    await page.locator('.modal').locator('input[placeholder="e.g. Bunt, Cluj-Napoca"]').fill('Test Cafe, Cluj')
    await page.locator('.modal').locator('input[type="datetime-local"]').fill('2026-12-01T10:00')
    await page.locator('.modal').locator('input[placeholder="90"]').fill('60')
    await page.locator('.modal').locator('input[placeholder="53"]').fill('1')
    await page.locator('.modal').locator('input[placeholder="Alex M."]').fill('testuser')
    await page.locator('.modal').locator('input[placeholder="300"]').fill('57')

    await page.click('button:has-text("Create Meet-up")')
    await expect(page.locator('.modal')).not.toBeVisible()

    await page.fill('.search-input', 'Playwright')
    await expect(page.locator('table.meetup-table').locator('text=Playwright Test Meet-up')).toBeVisible()
  })

  test('2.3 — Utilizatorul poate edita un meet-up existent', async ({ page }) => {
    await page.locator('.action-btn--edit').first().click()
    await expect(page.locator('.modal')).toBeVisible()

    // Locație e al doilea câmp text din modal
    const locationInput = page.locator('.modal').locator('input[placeholder="e.g. Bunt, Cluj-Napoca"]')
    await locationInput.clear()
    await locationInput.fill('Edited Location, Cluj')

    await page.click('button:has-text("Save Changes")')
    await expect(page.locator('.modal')).not.toBeVisible()

    await expect(page.locator('table.meetup-table').locator('text=Edited Location, Cluj')).toBeVisible()
  })

  test('2.4 — Utilizatorul poate șterge un meet-up', async ({ page }) => {
    // Reține ID-ul primului rând
    const firstRowText = await page.locator('table.meetup-table tbody tr').first().textContent()

    await page.locator('.action-btn--delete').first().click()
    await expect(page.locator('.confirm-box')).toBeVisible()
    await page.click('button:has-text("Delete")')
    await expect(page.locator('.confirm-box')).not.toBeVisible()

    // Verifică că elementul șters nu mai apare
    const firstRowAfter = await page.locator('table.meetup-table tbody tr').first().textContent()
    expect(firstRowAfter).not.toBe(firstRowText)
  })

  test('2.5 — Click pe rând deschide detail panel', async ({ page }) => {
    await page.locator('table.meetup-table tbody tr').first().click()

    await expect(page.locator('.detail-panel')).toBeVisible()
    await expect(page.locator('text=Meet-up Details')).toBeVisible()

    await page.click('.detail-panel__close')
    await expect(page.locator('.detail-panel')).not.toBeVisible()
  })
})

// ════════════════════════════════════════════════════════════
//  SCENARIUL 3: Validare Formulare
// ════════════════════════════════════════════════════════════
test.describe('Scenariul 3: Validare Formulare', () => {

  test.beforeEach(async ({ page }) => {
    await clearSession(page)
  })

  test('3.1 — Register: câmpuri goale afișează erori', async ({ page }) => {
    await page.goto('/register')
    await page.click('button:has-text("REGISTER")')

    await expect(page.locator('text=Full name is required')).toBeVisible()
    await expect(page.locator('text=Username is required')).toBeVisible()
    await expect(page.locator('text=Email is required')).toBeVisible()
    await expect(page.locator('text=Password is required')).toBeVisible()
  })

  test('3.2 — Register: email invalid afișează eroare', async ({ page }) => {
    await page.goto('/register')
    const inputs = page.locator('.auth-form input')
    await inputs.nth(2).fill('not-an-email')  // Email e al treilea input
    await inputs.nth(2).blur()

    await expect(page.locator('text=valid email')).toBeVisible()
  })

  test('3.3 — Register: parolele diferite afișează eroare', async ({ page }) => {
    await page.goto('/register')
    const inputs = page.locator('.auth-form input')
    await inputs.nth(3).fill('password123')  // Password
    await inputs.nth(4).fill('altceva999')   // Confirm Password
    await inputs.nth(4).blur()

    await expect(page.locator('text=Passwords do not match')).toBeVisible()
  })

  test('3.4 — Register: username cu caractere speciale afișează eroare', async ({ page }) => {
    await page.goto('/register')
    const inputs = page.locator('.auth-form input')
    await inputs.nth(1).fill('user name!')  // Username e al doilea input
    await inputs.nth(1).blur()

    await expect(page.locator('text=letters, numbers and underscores')).toBeVisible()
  })

  test('3.5 — Login: câmpuri goale afișează erori', async ({ page }) => {
    await page.goto('/login')
    await page.click('button:has-text("LOGIN")')

    await expect(page.locator('text=Email is required')).toBeVisible()
    await expect(page.locator('text=Password is required')).toBeVisible()
  })

  test('3.6 — Modal meet-up: câmpuri goale afișează erori', async ({ page }) => {
    await loginAsAdmin(page)

    await page.click('button:has-text("New Meet-up")')
    await expect(page.locator('.modal')).toBeVisible()
    await page.click('button:has-text("Create Meet-up")')

    await expect(page.locator('text=Event title is required')).toBeVisible()
    await expect(page.locator('text=Location is required')).toBeVisible()
    await expect(page.locator('text=Date is required')).toBeVisible()
  })

  test('3.7 — Modal meet-up: dată în trecut afișează eroare', async ({ page }) => {
    await loginAsAdmin(page)

    await page.click('button:has-text("New Meet-up")')
    await expect(page.locator('.modal')).toBeVisible()

    const modalInputs = page.locator('.modal input')
    await modalInputs.nth(2).fill('2020-01-01T10:00')  // date e al treilea input
    await modalInputs.nth(2).blur()

    await expect(page.locator('text=cannot be in the past')).toBeVisible()
  })
})
