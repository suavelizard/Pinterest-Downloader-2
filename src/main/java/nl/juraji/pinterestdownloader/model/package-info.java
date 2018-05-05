@TypeDefs(
        // Custom type persistence definitions for Hibernate
        @TypeDef(
                name = "File",
                defaultForType = java.io.File.class,
                typeClass = nl.juraji.pinterestdownloader.util.persistence.types.FileType.class
        )
)
package nl.juraji.pinterestdownloader.model;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;