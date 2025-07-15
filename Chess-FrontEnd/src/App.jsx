import { Route, Routes } from "react-router-dom";
import routes from "./Routes.jsx";

function App() {
  return (
    <>
    <Routes>
      {
        routes.map((route,index) => (
            <Route key={index} path={route.path} element={route.element} />
        ))
      } 
      </Routes>
    </>
  );
}

export default App;

export const API_CONTEXT_PATH = "http://localhost:8080/api";