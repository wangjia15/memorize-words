import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'
import { Button } from '../components/ui/button'

export default function Home() {
  return (
    <div className="container mx-auto px-4 py-8">
      <div className="text-center mb-8">
        <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2">
          Welcome to Memorize Words
        </h1>
        <p className="text-xl text-gray-600 dark:text-gray-300">
          Your personal vocabulary learning companion
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Learn</CardTitle>
            <CardDescription>
              Add new words to your vocabulary
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button className="w-full">Start Learning</Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Practice</CardTitle>
            <CardDescription>
              Review words with spaced repetition
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button className="w-full" variant="outline">Practice Now</Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Progress</CardTitle>
            <CardDescription>
              Track your learning journey
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button className="w-full" variant="outline">View Stats</Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}