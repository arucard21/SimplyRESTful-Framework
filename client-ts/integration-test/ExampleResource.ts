import { HALResource } from './../src/HALResource';

export type ExampleResource<T extends HALResource = HALResource> = {
    description: string;
    complexAttribute: {
        name: string;
    };
} & T;