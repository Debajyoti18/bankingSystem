# Kubernetes Deployment

This folder contains Kubernetes manifests for the Banking System backend and frontend.

## Files

- `backend-deployment.yaml` - deployment and service for Spring Boot backend
- `frontend-deployment.yaml` - deployment and service for React frontend

## Usage

1. Build container images for backend and frontend.
2. Push images to a container registry.
3. Update `image:` values in the YAML files.
4. Apply manifests:
   ```bash
   kubectl apply -f k8s/backend-deployment.yaml
   kubectl apply -f k8s/frontend-deployment.yaml
   ```

## Notes

- Backend service exposes port `8080` internally.
- Frontend service uses `LoadBalancer` and routes to `5173` inside the container.
- Replace placeholder image names with your actual registry tags.
