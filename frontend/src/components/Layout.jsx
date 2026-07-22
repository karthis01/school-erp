import Sidebar from './Sidebar';
import Topbar from './Topbar';

export default function Layout({ title, eyebrow, children }) {
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="main-area">
        <Topbar title={title} eyebrow={eyebrow} />
        <div className="content">{children}</div>
      </div>
    </div>
  );
}
