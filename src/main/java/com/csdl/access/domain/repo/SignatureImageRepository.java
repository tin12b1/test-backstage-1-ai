package com.csdl.access.domain.repo;

import com.csdl.access.domain.SignatureImage;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository quan ly entity SignatureImage (anh chu ky). */
public interface SignatureImageRepository extends JpaRepository<SignatureImage, Long> {
}
