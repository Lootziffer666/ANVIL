import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import Overview from './pages/Overview'
import Providers from './pages/Providers'
import Models from './pages/Models'
import Playground from './pages/Playground'
import Config from './pages/Config'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Overview />} />
          <Route path="providers" element={<Providers />} />
          <Route path="models" element={<Models />} />
          <Route path="playground" element={<Playground />} />
          <Route path="config" element={<Config />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
