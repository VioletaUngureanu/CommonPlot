<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const scrollY = ref(0)
const heroVisible = ref(false)
const whoVisible = ref(false)
const storyVisible = ref(false)

const handleScroll = () => {
  scrollY.value = window.scrollY
}

const observeSection = (id: string, visibleRef: { value: boolean }) => {
  const el = document.getElementById(id)
  if (!el) return
  const observer = new IntersectionObserver(
    ([entry]) => { if (entry.isIntersecting) visibleRef.value = true },
    { threshold: 0.2 }
  )
  observer.observe(el)
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  setTimeout(() => { heroVisible.value = true }, 100)
  observeSection('who-section', whoVisible)
  observeSection('story-section', storyVisible)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const goToRegister = () => router.push('/register')
</script>

<template>
  <div class="landing">

    <!-- HERO -->
    <section class="hero" :class="{ visible: heroVisible }">
      <div class="hero__image">
        <img src="../assets/bookshelf.png" alt="bookshelf" />
      </div>

      <div class="hero__content">
        <h1 class="hero__tagline">We read <em>alone</em>,</h1>
        <h1 class="hero__tagline hero__tagline--right">we grow <strong>together</strong></h1>
        <div class="hero__cta">
          <button class="cp-btn" @click="goToRegister">Register</button>
        </div>
      </div>
    </section>

    <!-- WHO ARE WE -->
    <section id="who-section" class="who" :class="{ visible: whoVisible }">
      <div class="who__text">
        <h2>Who are we?</h2>
        <p>
          CommonPlot is a social hub, where the books from your library become
          coffee dates and conversation starters. Meet new people, with whom you
          share interests and passions and transform solitary reading into a
          community experience right in your neighborhood.
        </p>
        <div class="who__cta">
          <button class="cp-btn cp-btn--outline-light" @click="goToRegister">Register</button>
        </div>
      </div>
      <div class="who__illustration">
        <img src="../assets/peopleTalking1.png" alt="people talking" />
      </div>
    </section>

    <!-- OUR STORY -->
    <section id="story-section" class="story" :class="{ visible: storyVisible }">
      <div class="story__illustration">
        <img src="../assets/peopleReading.png" alt="people reading" />
      </div>
      <div class="story__text">
        <h2>Our story</h2>
        <p>
          CommonPlot has been born from a desire to connect with other people
          and to discuss the books that we consume. It's difficult to find a
          community as an adult, when life is a zigzag of home and work. We
          want our platform to help create a space where people can link up and
          talk about the books they love, building a space that encourages deep
          thought and human connection.
        </p>
        <div class="story__cta">
          <button class="cp-btn" @click="goToRegister">Register</button>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <span class="logo-main">Common</span><span class="logo-accent">Plot</span>
      <p>© 2026 CommonPlot. All rights reserved.</p>
    </footer>

  </div>
</template>

<style scoped>
/* ── Local tokens ── */
.landing {
  --burgundy-500: #8B1A2F;
  --burgundy-600: #7a1628;
  --burgundy-700: #621220;
  --burgundy-900: #2a0a10;
  --cream-100:    #FFFDF7;
  --cream-200:    #FAF6EC;
  --cream-300:    #F2EBD3;
  --gold-500:     #D4A017;
  font-family: 'Playfair Display', Georgia, serif;
  background: var(--cream-100);
  color: var(--burgundy-900);
  overflow-x: hidden;
}

/* ══════════════════════════════════════════
   BUTON UNIC — UNITY
   Același pe toate cele 3 secțiuni.
   display:inline-block + width:fit-content
   împiedică întinderea pe toată lățimea.
══════════════════════════════════════════ */
.cp-btn {
  display: inline-block;
  width: fit-content;
  background: var(--burgundy-500);
  color: var(--cream-100);
  border: 2px solid var(--burgundy-500);
  padding: 0.65rem 2rem;
  border-radius: 9999px;
  font-family: 'Inter', sans-serif;
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s, color 0.2s;
}
.cp-btn:hover {
  background: var(--burgundy-700);
  border-color: var(--burgundy-700);
  transform: translateY(-1px);
}

/* Varianta pentru fundal întunecat */
.cp-btn--outline-light {
  background: transparent;
  color: var(--cream-100);
  border-color: var(--cream-100);
}
.cp-btn--outline-light:hover {
  background: var(--cream-100);
  color: var(--burgundy-700);
  border-color: var(--cream-100);
}

/* ══════════════════════════
   HERO
══════════════════════════ */
.hero {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: stretch;
  background: var(--cream-100);
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.8s ease, transform 0.8s ease;
}
.hero.visible { opacity: 1; transform: translateY(0); }

/* Fix gap: display:block + line-height:0 elimină spațiul
   implicit pe care browserul îl adaugă sub orice <img> inline */
.hero__image {
  display: flex;
  align-items: flex-end;
  padding-left: 3rem;
  line-height: 0;
  overflow: hidden;
}
.hero__image img {
  display: block;
  max-width: 100%;
  object-fit: contain;
  object-position: bottom;
}

.hero__content {
  padding: 4rem 3rem;
  display: flex;
  flex-direction: column;
  gap: 1.4rem;
  justify-content: center;
}
.hero__cta { display: flex; justify-content: flex-start; }

.hero__tagline {
  font-size: clamp(2rem, 4vw, 3rem);
  line-height: 1.25;
  color: var(--burgundy-900);
  margin: 0;
}
.hero__tagline--right { text-align: right; }
.hero__tagline em     { font-style: normal; color: var(--gold-500); }
.hero__tagline strong { font-style: italic; color: var(--burgundy-700); font-weight: 700; }

/* ══════════════════════════
   WHO ARE WE
══════════════════════════ */
.who {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: center;
  background: var(--burgundy-500);
  color: var(--cream-100);
  padding: 5rem 4rem;
  gap: 3rem;
  opacity: 0;
  transform: translateY(40px);
  transition: opacity 0.8s ease 0.1s, transform 0.8s ease 0.1s;
}
.who.visible { opacity: 1; transform: translateY(0); }

.who__text {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}
.who__text h2 {
  font-size: clamp(2rem, 3.5vw, 2.8rem);
  color: var(--cream-100);
}
.who__text p {
  font-family: 'Inter', sans-serif;
  font-size: 0.95rem;
  line-height: 1.75;
  color: var(--cream-300);
}
.who__cta { display: flex; }

.who__illustration img {
  width: 100%;
  max-width: 500px;
  height: auto;
  display: block;
  margin: 0 auto;
}

/* ══════════════════════════
   OUR STORY
══════════════════════════ */
.story {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: center;
  background: var(--cream-200);
  padding: 5rem 4rem;
  gap: 3rem;
  opacity: 0;
  transform: translateY(40px);
  transition: opacity 0.8s ease 0.1s, transform 0.8s ease 0.1s;
}
.story.visible { opacity: 1; transform: translateY(0); }

.story__illustration img {
  width: 100%;
  max-width: 500px;
  height: auto;
  display: block;
  margin: 0 auto;
}
.story__text {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
  padding-right: 2rem;
}
.story__text h2 {
  font-size: clamp(2rem, 3.5vw, 2.8rem);
  color: var(--burgundy-500);
}
.story__text p {
  font-family: 'Inter', sans-serif;
  font-size: 0.95rem;
  line-height: 1.75;
  color: var(--burgundy-700);
}
.story__cta { display: flex; }

/* ══════════════════════════
   FOOTER
══════════════════════════ */
.footer {
  background: var(--burgundy-900);
  color: var(--cream-300);
  text-align: center;
  padding: 2rem;
  font-family: 'Inter', sans-serif;
  font-size: 0.8rem;
}
.footer .logo-main   { color: var(--cream-100); font-size: 1rem; }
.footer .logo-accent { color: var(--gold-500);  font-size: 1rem; }
.footer p { margin-top: 0.5rem; opacity: 0.5; }

/* ══════════════════════════
   RESPONSIVE
══════════════════════════ */
@media (max-width: 768px) {
  .hero, .who, .story      { grid-template-columns: 1fr; padding: 3rem 1.5rem; }
  .hero__content            { padding: 2.5rem 1.5rem; }
  .who__illustration,
  .story__illustration      { order: -1; }
}
</style>
