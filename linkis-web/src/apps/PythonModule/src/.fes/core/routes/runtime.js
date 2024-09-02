import { createRouter } from "./routeExports";

export function onAppCreated({ app, routes }) {
    const router = createRouter(routes);
    app.use(router);
}
