import { HALResource } from './../src/HALResource';

export type ExampleResource = HALResource & {
    description: string;
    complexAttribute: {
        name: string;
    };
}
