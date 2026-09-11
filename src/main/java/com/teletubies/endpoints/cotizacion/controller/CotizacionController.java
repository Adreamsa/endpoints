package com.teletubies.endpoints.cotizacion.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/cotizacion")
public class CotizacionController {

    private final CourseService courseService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @GetMapping
    public ResponseEntity<CotizacionService.PagedResult<Cotizacion>> listCotizacion(
            @RequestParam(required = false) String instructor,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(cotizacionService.listCourses(instructor, page, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cotizacion> getCotizacionById(@PathVariable String id) {
        return ResponseEntity.ok(otizacionService.getCotizacionById(id));
    }

    // POST /cotizacions
    @PostMapping
    public ResponseEntity<Cotizacion> createCotizacion(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String instructor = (String) body.get("instructor");
        Integer capacity = extractCapacity(body);

        if (capacity == null) {
            throw new IllegalArgumentException("capacity es requerido y debe ser numérico");
        }

        Cotizacion created = cotizacionService.createCotizacion(title, instructor, capacity);

        return ResponseEntity
                .created(URI.create("/cotizacions/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cotizacion> updateCotizacion(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String instructor = (String) body.get("instructor");
        Integer capacity = extractCapacity(body);

        if (capacity == null) {
            throw new IllegalArgumentException("capacity es requerido y debe ser numérico");
        }

        Cotizacion updated = cotizacionService.updateCotizacion(id, title, instructor, capacity);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Cotizacion> patchCotizacion(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String instructor = (String) body.get("instructor");
        Integer capacity = extractCapacity(body);

        Cotizacion patched = cotizacionService.patchCotizacion(id, title, instructor, capacity);
        return ResponseEntity.ok(patched);
    }

    // DELETE /cotizacions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCotizacion(@PathVariable String id) {
        cotizacionService.deleteCotizacion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enrollments")
    public ResponseEntity<Enrollment> enrollStudent(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String studentName = body.get("studentName");

        Enrollment enrollment = cotizacionService.enrollStudent(id, studentName);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/cotizacion/" + id + "/enrollments/" + enrollment.getId())
                .body(enrollment);
    }

    private Integer extractCapacity(Map<String, Object> body) {
        Object capacity = body.get("capacity");
        if (capacity == null) {
            return null;
        }
        if (capacity instanceof Number) {
            return ((Number) capacity).intValue();
        }
        throw new IllegalArgumentException("capacity debe ser un número");
    }
}