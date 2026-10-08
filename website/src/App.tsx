import { TEAM } from "./crew";

/** Phase-1 placeholder Home page. The full website is built in Phase 4. */
export default function App() {
  return (
    <main className="home">
      <h1>TrustOrbit</h1>
      <p className="lede">
        Satellite and geospatial data contain noise, gaps and gradual sensor drift that often go
        unnoticed. TrustOrbit scores every ISRO satellite record from 0 to 100 for reliability and
        explains why.
      </p>
      <p className="status">The dashboard is under development.</p>
      <footer>
        <p>{TEAM.map((m) => m.name).join(" · ")}</p>
        <p>Data Source MOSDAC/SAC/ISRO. https://mosdac.gov.in</p>
      </footer>
    </main>
  );
}
