import { test, expect } from '@playwright/test';

test.describe('Silver Challenge - E2E Scenarios', () => {

    test.beforeEach(async ({ page }) => {
        // Pornim de la pagina de login pentru fiecare test
        await page.goto('http://localhost:5173/login');
    });

    // --- SCENARIUL 1: Autentificare și Protecția Rutelor ---
    test('should login successfully and redirect to meetups', async ({ page }) => {
        await page.fill('input[type="email"]', 'admin@commonplot.com');
        await page.fill('input[type="password"]', 'admin123');
        await page.click('.auth-btn');

        // Verificăm redirecționarea către pagina de meetups (cerință Gold/Silver transitions)
        await expect(page).toHaveURL(/\/meetups/);
        await expect(page.locator('.topbar__title')).toContainText('Admin: Meet-ups Management');
    });

    // --- SCENARIUL 2: CRUD - Crearea unui Meet-up nou ---
    test('should create a new meetup and display it in the list', async ({ page }) => {
        // Login prealabil
        await page.fill('input[type="email"]', 'admin@commonplot.com');
        await page.fill('input[type="password"]', 'admin123');
        await page.click('.auth-btn');

        // Deschidere modal adăugare
        await page.click('.btn-new');

        // Completare formular modal (MeetupModal.vue)
        await page.fill('input[placeholder*="Morning Coffee"]', 'Playwright Test Event');
        await page.fill('input[placeholder*="Bunt"]', 'Online Library');
        // Setăm o dată în viitor pentru a trece de validare
        await page.fill('input[type="datetime-local"]', '2026-10-10T10:00');
        await page.fill('input[type="number"] >> nth=0', '90'); // Duration
        await page.fill('input[type="number"] >> nth=1', '1');  // Owner ID
        await page.fill('input[type="text"] >> nth=2', 'tester_user'); // Username
        await page.fill('input[type="number"] >> nth=2', '500'); // Book ID

        await page.click('.btn-primary'); // Save

        // Verificăm prezența în tabel
        await expect(page.locator('.meetup-table')).toContainText('Playwright Test Event');
    });

    // --- SCENARIUL 3: Monitorizarea activității prin Cookies (Cerință Silver)  ---
    test('should track user visit count in cookies', async ({ page, context }) => {
        // Prima vizită și login
        await page.fill('input[type="email"]', 'admin@commonplot.com');
        await page.fill('input[type="password"]', 'admin123');
        await page.click('.auth-btn');

        // Verificăm dacă s-a creat cookie-ul de sesiune și cel de vizită
        const cookies = await context.cookies();
        const sessionCookie = cookies.find(c => c.name === 'commonplot_session');
        const visitCountCookie = cookies.find(c => c.name === 'commonplot_visit_count');

        expect(sessionCookie).toBeDefined();
        expect(visitCountCookie).toBeDefined();

        // Verificăm dacă numărul de vizite este cel puțin 1
        const count = parseInt(visitCountCookie?.value || '0');
        expect(count).toBeGreaterThan(0);
    });

    // --- SCENARIUL EXTRA: Validare Client-Side (Cerință Bronze/Silver) [cite: 2] ---
    test('should show validation errors on empty login', async ({ page }) => {
        await page.click('.auth-btn');

        const emailError = page.locator('.field-error >> nth=0');
        const passwordError = page.locator('.field-error >> nth=1');

        await expect(emailError).toBeVisible();
        await expect(emailError).toContainText('Email is required');
        await expect(passwordError).toContainText('Password is required');
    });
});