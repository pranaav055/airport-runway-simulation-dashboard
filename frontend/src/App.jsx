import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import ConfigPage from "./pages/ConfigPage.jsx";
import MetricsPage from "./pages/MetricsPage.jsx";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<ConfigPage />} />
        <Route path="/metrics" element={<MetricsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
