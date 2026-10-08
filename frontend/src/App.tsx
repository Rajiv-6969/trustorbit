import { CREW } from "./crew";

/** Phase-1 placeholder Launch pad. The full UI arrives in Phase 4. */
export default function App() {
  return (
    <main className="launch">
      <h1>TrustOrbit</h1>
      <p className="tagline">Not all satellite data is created equal.</p>
      <p>Mission Control is under construction. Pinging satellites…</p>
      <footer>Built by {CREW.map((m) => m.name).join(" · ")}</footer>
    </main>
  );
}
