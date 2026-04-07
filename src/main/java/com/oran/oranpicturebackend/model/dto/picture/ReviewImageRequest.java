package com.oran.oranpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
class ReviewImageRequest implements Serializable {
        private String imageUrl;
        private static final long serialVersionUID = 1L;
    }