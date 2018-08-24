# Changelog

## 0.0.2

- [ADDED] Particle emitter component in Scene
- [ADDED] Lit particles in ParticleRenderer
- [ADDED] ResourcePath class to unify file access
- [ADDED] Shader #import directive

## 0.0.3
- [ADDED] Scene json model is now extensible
- [ADDED] Scene json model can now contain particle component
- [REMOVED] All Spritebatch::draw methods
- [ADDED] Spritebatch::render that take the new Sprite class as parameter
- [ADDED] Asset management

### 0.0.4
- [CHANGED] Modularized the project
- [ADDED] Partial transparency rendering support

### 0.0.5
- [OPTIMIZATION] Only render visible meshes
- [IMPROVEMENT] Added Cascaded Shadow Mapping
- [FIX] Fix GlTF model loading
- [ADDED] Generic animation API
- [ADDED] Model's transform based animations
- [ADDED] GlTf transform based animations loading
