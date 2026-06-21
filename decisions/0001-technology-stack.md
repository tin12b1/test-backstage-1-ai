# ADR 0001 - Chọn nền tảng Spring Boot + Thymeleaf Monolith

## Status

Accepted

## Context

Tài liệu nghiệp vụ chốt nền tảng giai đoạn đầu là Monolith: Spring Boot + Thymeleaf.

## Decision

Sử dụng kiến trúc monolith với Spring Boot và Thymeleaf cho cả backend và giao diện server-side rendering.

## Consequences

- Đơn giản hóa triển khai giai đoạn đầu.
- Phù hợp với các form nghiệp vụ, dashboard, phê duyệt nội bộ.
- Không tách microservice khi chưa có yêu cầu rõ ràng.
- Các module vẫn phải tách package/layer rõ ràng để dễ bảo trì.
