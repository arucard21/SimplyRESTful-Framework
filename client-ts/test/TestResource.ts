import { HALResource } from './../src/HALResource';

export type TestResource<T extends HALResource = HALResource> = {
    additionalField?: string;
} & T;
