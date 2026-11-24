import { useState } from "react";
import Sidebar from "./components/Sidebar.jsx";
import TopBar from "./components/TopBar.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import Players from "./pages/Players.jsx";
import Coaches from "./pages/Coaches.jsx";
import Teams from "./pages/Teams.jsx";
import Tournaments from "./pages/Tournaments.jsx";
import Facilities from "./pages/Facilities.jsx";
import Equipment from "./pages/Equipment.jsx";
import Matches from "./pages/Matches.jsx";
import Analytics from "./pages/Analytics.jsx";
import Reports from "./pages/Reports.jsx";
import DataControls from "./pages/DataControls.jsx";
import PreviewBanner from "./components/PreviewBanner.jsx";

const PAGES = {
  dashboard: Dashboard,
  players: Players,
  coaches: Coaches,
  teams: Teams,
  tournaments: Tournaments,
  facilities: Facilities,
  equipment: Equipment,
  matches: Matches,
  analytics: Analytics,
  reports: Reports,
  data: DataControls,
};

export default function App() {
  const [page, setPage] = useState("dashboard");
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const Page = PAGES[page];

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">
        Skip to content
      </a>
      <Sidebar
        page={page}
        onNavigate={(p) => {
          setPage(p);
          setSidebarOpen(false);
        }}
        open={sidebarOpen}
      />
      <div className="app-main">
        <TopBar
          title={Page.title}
          icon={Page.icon}
          onToggleSidebar={() => setSidebarOpen((o) => !o)}
        />
        <PreviewBanner />
        <main id="main-content" className="page" key={page}>
          <Page />
        </main>
      </div>
    </div>
  );
}
