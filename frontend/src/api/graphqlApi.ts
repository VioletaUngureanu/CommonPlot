// src/api/graphqlApi.ts
import { GraphQLClient, gql } from 'graphql-request'

const client = new GraphQLClient('/api/graphql')

// ── Queries ──────────────────────────────────────────────────

export async function gqlFetchMeetups(page = 0, size = 10) {
  const query = gql`
    query GetMeetups($page: Int, $size: Int) {
      meetups(page: $page, size: $size) {
        content {
          id titleEvent location date
          bookID bookTitle bookAuthor
          ownerID ownerUsername duration rating description
        }
        page pageSize totalElements totalPages first last
      }
    }
  `
  const data: any = await client.request(query, { page, size })
  return data.meetups
}

export async function gqlFetchMeetupById(id: number) {
  const query = gql`
    query GetMeetup($id: ID!) {
      meetup(id: $id) {
        id titleEvent location date
        bookID bookTitle bookAuthor
        ownerID ownerUsername duration rating description
      }
    }
  `
  const data: any = await client.request(query, { id })
  return data.meetup
}

// ── Mutations ─────────────────────────────────────────────────

export async function gqlCreateMeetup(payload: any) {
  const mutation = gql`
    mutation CreateMeetup($input: MeetupInput!) {
      createMeetup(input: $input) {
        id titleEvent location date bookID ownerID duration rating
      }
    }
  `
  const data: any = await client.request(mutation, { input: payload })
  return data.createMeetup
}

export async function gqlUpdateMeetup(id: number, payload: any) {
  const mutation = gql`
    mutation UpdateMeetup($id: ID!, $input: MeetupInput!) {
      updateMeetup(id: $id, input: $input) {
        id titleEvent location date bookID ownerID duration rating
      }
    }
  `
  const data: any = await client.request(mutation, { id, input: payload })
  return data.updateMeetup
}

export async function gqlDeleteMeetup(id: number) {
  const mutation = gql`
    mutation DeleteMeetup($id: ID!) {
      deleteMeetup(id: $id)
    }
  `
  const data: any = await client.request(mutation, { id })
  return data.deleteMeetup
}
