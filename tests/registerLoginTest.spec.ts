import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:5173';

test.describe('Silver Challenge Scenarios', () => {

    // Rulăm login-ul înainte de testele de CRUD pentru a avea sesiunea activă
    test.beforeEach(async ({ page }) => {
        await page.goto(`${BASE_URL}/login`);
        await page.fill('input[type="email"]', 'admin@commonplot.com');
        await page.fill('input[type="password"]', 'admin123');
        await page.click('.auth-btn');
        // Așteptăm să ajungem pe pagina de meetups pentru a fi siguri că sesiunea e setată
        await expect(page).toHaveURL(/.*meetups/);
    });

    // SCENARIUL 1: Verificare Login (Deja confirmat de beforeEach, dar adăugăm un check de UI)
    test('utilizatorul este logat și vede titlul paginii de administrare', async ({ page }) => {
        await expect(page.locator('.topbar__title')).toContainText('Admin: Meet-ups Management');
    });

    // SCENARIUL 2: Gestiune Meet-ups (CRUD - Cerință Silver/Bronze) [cite: 2, 3]
    test('crearea unui meet-up nou apare în listă', async ({ page }) => {
        await page.click('.btn-new');

        const eventTitle = 'Playwright Event ' + Date.now(); // Nume unic pentru a evita duplicatele

        await page.fill('input[placeholder*="e.g. Morning Coffee"]', eventTitle);
        await page.fill('input[placeholder*="e.g. Bunt"]', 'Cluj Napoca');
        await page.fill('input[type="datetime-local"]', '2026-10-10T10:00');
        await page.fill('input[type="number"] >> nth=0', '120');
        await page.fill('input[type="number"] >> nth=1', '1');
        await page.fill('input[type="text"] >> nth=2', 'admin');
        await page.fill('input[type="number"] >> nth=2', '300');

        await page.click('.btn-primary');

        // --- ADAUGĂ ACEST PAS PENTRU SILVER ---
        // Folosim bara de căutare ca să fim siguri că item-ul apare pe prima pagină
        await page.fill('.search-input', eventTitle);

        // Verificăm rândul din tabel
        await expect(page.locator('.meetup-table tbody')).toContainText(eventTitle);
    });

    // SCENARIUL 3: Monitorizare activitate (Cookies - Cerință Silver obligatorie)
    test('verifică dacă sistemul de monitorizare a vizitelor funcționează', async ({ context }) => {
        // Deoarece am făcut login în beforeEach, cookie-ul trebuie să existe
        const cookies = await context.cookies();
        const visitCookie = cookies.find(c => c.name === 'commonplot_visit_count');
        const lastVisitCookie = cookies.find(c => c.name === 'commonplot_last_visit');

        expect(visitCookie).toBeDefined();
        expect(lastVisitCookie).toBeDefined();

        // Verificăm dacă valoarea este un număr valid (string în cookie)
        const count = parseInt(visitCookie?.value || '0');
        expect(count).toBeGreaterThan(0);
    });
});