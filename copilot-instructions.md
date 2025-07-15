A simple stage builder Java annotation processing library for Pojo Classes and Records.

## Code Standards

### Requirements 
- Minimum JDK is 21
- latest gradle

### Development Flow
- Build: `gradle assemble`
- Test: `gradle test`
- Full CI check: `gradle build` (includes build, test)

## Repository Structure
- `api`: Annotation definition
- `processor`: Annotation processor

## Key Guidelines
1. Follow Go best practices and idiomatic patterns
2. Maintain existing code structure and organization
3. Write unit tests for new functionality. Use table-driven unit tests when possible.
5. Document public APIs and complex logic. Suggest changes to the `README.md` when appropriate
