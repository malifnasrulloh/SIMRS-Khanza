/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author malifnasrulloh
 * @param <T>
 */
public class FHIRPayload<T extends IBaseResource> {

    private static final FhirContext CTX = FhirContext.forR4();

    private final T resource;

    private FHIRPayload(T resource) {
        this.resource = resource;
    }

    public static <T extends IBaseResource> FHIRPayload<T> from(T resource) {
        return new FHIRPayload<>(resource);
    }

    public String toJson() {
        return CTX.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }

    public String toXml() {
        return CTX.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
    }

    public T getResource() {
        return resource;
    }
}
