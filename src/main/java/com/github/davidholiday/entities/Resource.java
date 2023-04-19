package com.github.davidholiday.entities;

import jakarta.persistence.*;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.commons.codec.net.URLCodec;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@Entity
public class Resource implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Resource.class);

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();

    private static final URLCodec URL_CODEC = new URLCodec();

    private static final int DESCRIPTION_MIN_LENGTH = 1;

    private static final int DESCRIPTION_MAX_LENGTH = 256;

    private static final int URL_MIN_LENGTH = 1;

    private static final int URL_MAX_LENGTH = 2000;

    //

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(length=DESCRIPTION_MAX_LENGTH, nullable=false)
    private String description;

    // ty so https://stackoverflow.com/a/417184/2234770
    @Column(length=URL_MAX_LENGTH, nullable=false)
    private String url;

    //

    public Long getId() { return id; }

    //

    public String getDescription() { return description; }

    public void setDescription(@NotNull String description) {
        if (description.length() < DESCRIPTION_MIN_LENGTH || description.length() > DESCRIPTION_MAX_LENGTH) {
            LOG.error(
                    "refusing to set description to: {} because it does not meet size constraint between {} and {} chars",
                    description,
                    DESCRIPTION_MIN_LENGTH,
                    DESCRIPTION_MAX_LENGTH
            );
            throw new IllegalArgumentException();
        }

        this.description = StringEscapeUtils.escapeHtml4(description);
    }

    //

    public String getUrl() { return url; }

    public void setUrl(@NotNull String url) {
        if (url.length() < URL_MIN_LENGTH || url.length() > URL_MAX_LENGTH) {
            LOG.error(
                    "refusing to set url to: {} because it does not meet size constraint between {} and {} chars",
                    url,
                    URL_MIN_LENGTH,
                    URL_MAX_LENGTH
            );
            throw new IllegalArgumentException();
        } else if (URL_VALIDATOR.isValid(url) == false) {
            LOG.error("refusing to set url to: {} because it is not a valid url", url);
            throw new IllegalArgumentException();
        }

        try {
            this.url = URL_CODEC.encode(url);
        } catch (EncoderException e) {
            LOG.error("refusing to set url to: {} due to encoder exception {}", url, e);
            throw new IllegalArgumentException();
        }

    }

}
