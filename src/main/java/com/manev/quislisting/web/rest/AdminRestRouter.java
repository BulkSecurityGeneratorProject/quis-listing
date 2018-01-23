package com.manev.quislisting.web.rest;

public class AdminRestRouter {

    public class QlString {
        public static final String LIST = "/api/admin/ql-strings";
        public static final String DETAIL = "/api/admin/ql-strings/{id}";

        private QlString() {
        }
    }

    public class Language {

        public static final String LIST = "/api/admin/languages";
        public static final String DETAIL = "/api/admin/languages/{id}";

        private Language() {
        }
    }

    public class EmailTemplate {
        public static final String LIST = "/api/admin/email-templates";
        public static final String DETAIL = "/api/admin/email-templates/{id}";

        private EmailTemplate() {
        }
    }

    public class QlConfig {
        public static final String LIST = "/api/admin/ql-configurations";
        public static final String DETAIL = "/api/admin/ql-configurations/{id}";

        private QlConfig() {
        }
    }

    public class DlListing {
        public static final String LIST = "/api/admin/dl-listings";
        public static final String DETAIL = "/api/admin/dl-listings/{id}";
        public static final String PUBLISH = "/api/admin/dl-listings/publish";
        public static final String UPLOAD = "/api/admin/dl-listings/{id}/upload";
        public static final String APPROVE = "/api/admin/dl-listings/{id}/approve";
        public static final String DISAPPROVE = "/api/admin/dl-listings/{id}/disapprove";
        public static final String REBUILD_INDEX = "/api/admin/dl-listings/rebuild-index";
        public static final String ATTACHMENT_DETAIL = "/api/admin/dl-listings/{id}/attachments/{attachmentId}";
        public static final String ACTIVE_LANGUAGES = "/api/admin/dl-listings/active-languages";


        private DlListing() {
        }
    }

    public class DlContentFields {
        public static final String LIST = "/api/admin/dl-content-fields";
        public static final String DETAIL = "/api/admin/dl-content-fields/{id}";

        private DlContentFields() {
        }

        public class DlContentFieldItem {
            public static final String LIST = "/api/admin/dl-content-fields/{dlContentFieldId}/dl-content-field-items";
            public static final String DETAIL = "/api/admin/dl-content-fields/{dlContentFieldId}/dl-content-field-items/{id}";

            private DlContentFieldItem() {
            }
        }
    }

    public class DlContentFieldGroup {

        private DlContentFieldGroup() {
            // private constructor
        }

        public static final String LIST = "/api/admin/dl-content-field-groups";
        public static final String DETAIL = "/api/admin/dl-content-field-groups/{id}";

    }
}
